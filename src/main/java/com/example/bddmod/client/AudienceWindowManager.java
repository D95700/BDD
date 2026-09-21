package com.example.bddmod.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * Presents the audience texture in a small, separately capturable GLFW window.
 *
 * <p>The window shares Minecraft's OpenGL objects but has its own context and default
 * framebuffer. All calls are made on the render thread and the Minecraft context is restored
 * before returning to the normal frame pipeline.</p>
 */
public final class AudienceWindowManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("bddmod-audience-window");
    private static final String WINDOW_TITLE = "BDD Audience Output";
    private static final String HIDDEN_MESSAGE = "WE SEE YOU";
    private static final int GLYPH_WIDTH = 5;
    private static final int GLYPH_HEIGHT = 7;
    private static final String VERTEX_SHADER = "#version 150\n"
            + "in vec2 Position;\n"
            + "in vec2 UV;\n"
            + "out vec2 TexCoord;\n"
            + "void main() { gl_Position = vec4(Position, 0.0, 1.0); TexCoord = UV; }\n";
    private static final String FRAGMENT_SHADER = "#version 150\n"
            + "uniform sampler2D Texture;\n"
            + "uniform sampler2D HiddenText;\n"
            + "uniform float Pulse;\n"
            + "uniform float Time;\n"
            + "in vec2 TexCoord;\n"
            + "out vec4 FragColor;\n"
            + "float hash(vec2 value) {\n"
            + "    return fract(sin(dot(value, vec2(12.9898, 78.233))) * 43758.5453);\n"
            + "}\n"
            + "void main() {\n"
            + "    float cycle = Time * 6.2831853;\n"
            + "    vec2 distortionCenter = vec2(0.5 + 0.10 * sin(cycle),\n"
            + "            0.5 + 0.06 * cos(cycle * 0.5));\n"
            + "    vec2 distortionDelta = TexCoord - distortionCenter;\n"
            + "    vec2 distortionAspect = distortionDelta * vec2(1.0, 1.7777778);\n"
            + "    float distortionDistance = length(distortionAspect);\n"
            + "    float distortionMask = 1.0 - smoothstep(0.10, 0.34, distortionDistance);\n"
            + "    float distortionWave = sin(distortionDistance * 46.0 - cycle * 2.0);\n"
            + "    vec2 distortionDirection = normalize(distortionAspect + vec2(0.00001));\n"
            + "    float distortionStrength = (0.0010 + 0.0030 * Pulse) * distortionMask;\n"
            + "    vec2 warpedUv = clamp(TexCoord + distortionDirection / vec2(1.0, 1.7777778)\n"
            + "            * distortionWave * distortionStrength, vec2(0.001), vec2(0.999));\n"
            + "    vec4 source = texture(Texture, warpedUv);\n"
            + "    vec2 block = floor(TexCoord * vec2(160.0, 90.0));\n"
            + "    float grain = hash(block + vec2(floor(Time * 24.0))) - 0.5;\n"
            + "    float edge = 1.0 - smoothstep(0.0, 0.32, min(min(TexCoord.x, 1.0 - TexCoord.x),\n"
            + "            min(TexCoord.y, 1.0 - TexCoord.y)));\n"
            + "    float amount = (0.004 + 0.018 * Pulse) * (0.15 + 0.85 * edge);\n"
            + "    vec3 color = source.rgb + vec3(grain * amount);\n"
            + "    color += vec3(0.018, 0.0, 0.0) * edge * Pulse;\n"
            + "    vec2 textUv = (TexCoord - vec2(0.62, 0.10)) / vec2(0.30, 0.07);\n"
            + "    float hiddenText = 0.0;\n"
            + "    if (all(greaterThanEqual(textUv, vec2(0.0)))\n"
            + "            && all(lessThanEqual(textUv, vec2(1.0)))) {\n"
            + "        hiddenText = texture(HiddenText, textUv).r;\n"
            + "    }\n"
            + "    float reveal = smoothstep(0.68, 0.74, Time)\n"
            + "            * (1.0 - smoothstep(0.88, 0.94, Time));\n"
            + "    float textAlpha = hiddenText * reveal * (0.22 + 0.28 * Pulse);\n"
            + "    color = mix(color, vec3(0.42, 0.035, 0.035), textAlpha);\n"
            + "    FragColor = vec4(color, source.a);\n"
            + "}\n";

    private static long window;
    private static long minecraftWindow;
    private static GLCapabilities minecraftCapabilities;
    private static GLCapabilities audienceCapabilities;
    private static int program;
    private static int vao;
    private static int vertexBuffer;
    private static int indexBuffer;
    private static int hiddenTextTexture;
    private static int textureUniform;
    private static int hiddenTextUniform;
    private static int pulseUniform;
    private static int timeUniform;
    private static boolean failed;
    private static int windowWidth;
    private static int windowHeight;

    private AudienceWindowManager() {
    }

    public static boolean present(Minecraft minecraft, RenderTarget target) {
        RenderSystem.assertOnRenderThread();
        if (failed || target == null || !AudienceRenderTargetManager.isReady()) {
            return false;
        }

        long mainHandle = minecraft.getWindow().getWindow();
        if (mainHandle == 0L) {
            return false;
        }

        try {
            ensureWindow(mainHandle, target.width, target.height);
            if (GLFW.glfwWindowShouldClose(window)) {
                failed = true;
                LOGGER.warn("Audience output window was closed; output disabled until the next recording");
                releaseWindow(mainHandle);
                return false;
            }
            drawTexture(mainHandle, target.getColorTextureId(),
                    (float) RecordingPulseController.visualLevel(),
                    (float) RecordingPulseController.phase());
            return true;
        } catch (Throwable throwable) {
            failed = true;
            LOGGER.warn("Audience output window disabled after an OpenGL failure", throwable);
            releaseWindow(mainHandle);
            return false;
        }
    }

    public static String describeState() {
        if (failed) {
            return "FAILED";
        }
        if (window == 0L) {
            return "NOT OPEN";
        }
        return "OPEN " + windowWidth + "x" + windowHeight;
    }

    public static void release() {
        if (window == 0L && !failed) {
            return;
        }
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(AudienceWindowManager::releaseOnRenderThread);
            return;
        }
        releaseOnRenderThread();
    }

    private static void ensureWindow(long mainHandle, int targetWidth, int targetHeight) {
        if (window != 0L) {
            return;
        }

        minecraftWindow = mainHandle;
        minecraftCapabilities = GL.getCapabilities();
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        int initialWidth = Math.max(480, Math.min(1280, targetWidth));
        int initialHeight = Math.max(270, Math.min(720, targetHeight));
        window = GLFW.glfwCreateWindow(initialWidth, initialHeight, WINDOW_TITLE, 0L, mainHandle);
        if (window == 0L) {
            throw new IllegalStateException("GLFW could not create the shared audience window");
        }

        GLFW.glfwSetWindowPos(window, 32, 32);
        GLFW.glfwMakeContextCurrent(window);
        audienceCapabilities = GL.createCapabilities();
        GLFW.glfwSwapInterval(1);
        createPipeline();
        refreshWindowSize();
        GLFW.glfwShowWindow(window);
        restoreMinecraftContext();
        LOGGER.info("Audience output window opened at {}x{}; OBS can capture the '{}' window",
                windowWidth, windowHeight, WINDOW_TITLE);
    }

    private static void drawTexture(long mainHandle, int textureId, float pulse, float phase) {
        GLFW.glfwMakeContextCurrent(window);
        GL.setCapabilities(audienceCapabilities);
        refreshWindowSize();
        if (windowWidth <= 0 || windowHeight <= 0) {
            restoreMinecraftContext();
            return;
        }

        GL11.glViewport(0, 0, windowWidth, windowHeight);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glClearColor(0.015F, 0.015F, 0.015F, 1.0F);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        GL20.glUseProgram(program);
        GL30.glBindVertexArray(vao);
        GL13Compat.activeTexture1();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, hiddenTextTexture);
        GL20.glUniform1i(hiddenTextUniform, 1);
        GL13Compat.activeTexture0();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL20.glUniform1i(textureUniform, 0);
        GL20.glUniform1f(pulseUniform, pulse);
        GL20.glUniform1f(timeUniform, phase);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
        GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0L);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL13Compat.activeTexture1();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL13Compat.activeTexture0();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL30.glBindVertexArray(0);
        GL20.glUseProgram(0);
        GLFW.glfwSwapBuffers(window);
        restoreMinecraftContext();
    }

    private static void createPipeline() {
        int vertexShader = compileShader(GL20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = compileShader(GL20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertexShader);
        GL20.glAttachShader(program, fragmentShader);
        GL20.glBindAttribLocation(program, 0, "Position");
        GL20.glBindAttribLocation(program, 1, "UV");
        GL20.glLinkProgram(program);
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new IllegalStateException("Audience shader link failed: " + GL20.glGetProgramInfoLog(program));
        }
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
        textureUniform = GL20.glGetUniformLocation(program, "Texture");
        hiddenTextUniform = GL20.glGetUniformLocation(program, "HiddenText");
        pulseUniform = GL20.glGetUniformLocation(program, "Pulse");
        timeUniform = GL20.glGetUniformLocation(program, "Time");
        hiddenTextTexture = createHiddenTextTexture();

        float[] vertices = {
                -1.0F, -1.0F, 0.0F, 0.0F,
                 1.0F, -1.0F, 1.0F, 0.0F,
                 1.0F,  1.0F, 1.0F, 1.0F,
                -1.0F,  1.0F, 0.0F, 1.0F
        };
        int[] indices = {0, 1, 2, 2, 3, 0};
        vao = GL30.glGenVertexArrays();
        vertexBuffer = GL15.glGenBuffers();
        indexBuffer = GL15.glGenBuffers();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 16, 0L);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 16, 8L);
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private static int createHiddenTextTexture() {
        int textureWidth = HIDDEN_MESSAGE.length() * (GLYPH_WIDTH + 1) - 1;
        ByteBuffer pixels = BufferUtils.createByteBuffer(textureWidth * GLYPH_HEIGHT);
        for (int y = 0; y < GLYPH_HEIGHT; y++) {
            int glyphRow = GLYPH_HEIGHT - 1 - y;
            for (int characterIndex = 0; characterIndex < HIDDEN_MESSAGE.length(); characterIndex++) {
                int[] rows = glyphRows(HIDDEN_MESSAGE.charAt(characterIndex));
                for (int x = 0; x < GLYPH_WIDTH; x++) {
                    int bit = 1 << (GLYPH_WIDTH - 1 - x);
                    pixels.put((byte) ((rows[glyphRow] & bit) != 0 ? 0xFF : 0x00));
                }
                if (characterIndex < HIDDEN_MESSAGE.length() - 1) {
                    pixels.put((byte) 0x00);
                }
            }
        }
        pixels.flip();

        int texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_R8, textureWidth, GLYPH_HEIGHT, 0,
                GL11.GL_RED, GL11.GL_UNSIGNED_BYTE, pixels);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        return texture;
    }

    private static int[] glyphRows(char character) {
        return switch (character) {
            case 'W' -> new int[] {17, 17, 17, 21, 21, 27, 17};
            case 'E' -> new int[] {31, 16, 16, 30, 16, 16, 31};
            case 'S' -> new int[] {15, 16, 16, 14, 1, 1, 30};
            case 'Y' -> new int[] {17, 17, 10, 4, 4, 4, 4};
            case 'O' -> new int[] {14, 17, 17, 17, 17, 17, 14};
            case 'U' -> new int[] {17, 17, 17, 17, 17, 17, 14};
            default -> new int[] {0, 0, 0, 0, 0, 0, 0};
        };
    }

    private static int compileShader(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new IllegalStateException("Audience shader compile failed: " + GL20.glGetShaderInfoLog(shader));
        }
        return shader;
    }

    private static void refreshWindowSize() {
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetFramebufferSize(window, width, height);
        windowWidth = width[0];
        windowHeight = height[0];
    }

    private static void restoreMinecraftContext() {
        GLFW.glfwMakeContextCurrent(minecraftWindow);
        GL.setCapabilities(minecraftCapabilities);
    }

    private static void releaseOnRenderThread() {
        RenderSystem.assertOnRenderThread();
        long mainHandle = minecraftWindow != 0L ? minecraftWindow : Minecraft.getInstance().getWindow().getWindow();
        releaseWindow(mainHandle);
        failed = false;
    }

    private static void releaseWindow(long mainHandle) {
        if (window == 0L) {
            restoreMinecraftContextIfPossible(mainHandle);
            return;
        }

        GLFW.glfwMakeContextCurrent(window);
        GL.setCapabilities(audienceCapabilities);
        if (program != 0) {
            GL20.glDeleteProgram(program);
        }
        if (vertexBuffer != 0) {
            GL15.glDeleteBuffers(vertexBuffer);
        }
        if (indexBuffer != 0) {
            GL15.glDeleteBuffers(indexBuffer);
        }
        if (hiddenTextTexture != 0) {
            GL11.glDeleteTextures(hiddenTextTexture);
        }
        if (vao != 0) {
            GL30.glDeleteVertexArrays(vao);
        }
        GLFW.glfwDestroyWindow(window);
        window = 0L;
        program = 0;
        vertexBuffer = 0;
        indexBuffer = 0;
        hiddenTextTexture = 0;
        vao = 0;
        textureUniform = -1;
        hiddenTextUniform = -1;
        pulseUniform = -1;
        timeUniform = -1;
        windowWidth = 0;
        windowHeight = 0;
        restoreMinecraftContextIfPossible(mainHandle);
        LOGGER.info("Audience output window resources released");
    }

    private static void restoreMinecraftContextIfPossible(long mainHandle) {
        if (mainHandle != 0L && minecraftCapabilities != null) {
            GLFW.glfwMakeContextCurrent(mainHandle);
            GL.setCapabilities(minecraftCapabilities);
        }
    }

    /** Keeps the OpenGL 1.3 call isolated from the otherwise small manager API. */
    private static final class GL13Compat {
        private GL13Compat() {
        }

        private static void activeTexture0() {
            org.lwjgl.opengl.GL13.glActiveTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);
        }

        private static void activeTexture1() {
            org.lwjgl.opengl.GL13.glActiveTexture(org.lwjgl.opengl.GL13.GL_TEXTURE1);
        }
    }
}
