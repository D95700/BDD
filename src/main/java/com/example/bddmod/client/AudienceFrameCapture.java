package com.example.bddmod.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Copies the framebuffer that is currently presenting Minecraft's final frame into the
 * audience texture. The source is discovered from OpenGL state instead of from Minecraft's
 * vanilla render-target field so shader loaders can replace the final framebuffer.
 */
interface AudienceFrameCapture {
    CaptureResult capture(RenderTarget target);

    record CaptureResult(boolean success, String source, String failure) {
        static CaptureResult success(String source) {
            return new CaptureResult(true, source, "");
        }

        static CaptureResult failure(String failure) {
            return new CaptureResult(false, "CAPTURE_FAILED", failure);
        }
    }

    /** The default loader-neutral implementation used by Vanilla, Oculus, and Iris bridges. */
    final class CurrentFramebuffer implements AudienceFrameCapture {
        @Override
        public CaptureResult capture(RenderTarget target) {
            int previousRead = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
            int previousDraw = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
            int previousReadBuffer = GL11.glGetInteger(GL11.GL_READ_BUFFER);
            int previousDrawBuffer = GL11.glGetInteger(GL11.GL_DRAW_BUFFER);
            int previousActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
            int previousProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
            int previousVao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
            int previousArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
            int previousElementBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
            int[] viewport = new int[4];
            int[] scissor = new int[4];
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
            GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, scissor);
            boolean scissorEnabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);

            try {
                int sourceWidth = viewport[2];
                int sourceHeight = viewport[3];
                if (sourceWidth <= 0 || sourceHeight <= 0) {
                    return CaptureResult.failure("current framebuffer has an empty viewport");
                }

                // Minecraft may leave an unrelated GL error pending; only report errors caused
                // by this capture operation.
                while (GL11.glGetError() != GL11.GL_NO_ERROR) {
                    // Drain stale errors before the blit.
                }
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, previousRead);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, target.frameBufferId);
                GL30.glBlitFramebuffer(
                        0, 0, sourceWidth, sourceHeight,
                        0, 0, target.width, target.height,
                        GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
                int error = GL11.glGetError();
                if (error != GL11.GL_NO_ERROR) {
                    return CaptureResult.failure("glBlitFramebuffer returned OpenGL error 0x"
                            + Integer.toHexString(error));
                }
                ShaderCompatibility.reportCaptureSuccess();
                return CaptureResult.success("CURRENT_FRAMEBUFFER fbo=" + previousRead
                        + " " + sourceWidth + "x" + sourceHeight);
            } catch (Throwable throwable) {
                return CaptureResult.failure(throwable.getClass().getSimpleName() + ": "
                        + String.valueOf(throwable.getMessage()));
            } finally {
                GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, previousRead);
                GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previousDraw);
                GL11.glReadBuffer(previousReadBuffer);
                GL11.glDrawBuffer(previousDrawBuffer);
                GL11.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
                GL11.glScissor(scissor[0], scissor[1], scissor[2], scissor[3]);
                GL13.glActiveTexture(previousActiveTexture);
                GL20.glUseProgram(previousProgram);
                GL30.glBindVertexArray(previousVao);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, previousArrayBuffer);
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, previousElementBuffer);
                if (scissorEnabled) {
                    GL11.glEnable(GL11.GL_SCISSOR_TEST);
                } else {
                    GL11.glDisable(GL11.GL_SCISSOR_TEST);
                }
            }
        }
    }
}
