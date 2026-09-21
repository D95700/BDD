#!/usr/bin/env python3
"""Verify the installable Forge JAR produced by the current Gradle build."""

from __future__ import annotations

import argparse
import hashlib
import re
import sys
import zipfile
from pathlib import Path


SEMVER = re.compile(
    r"^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)"
    r"(?:-((?:0|[1-9A-Za-z-][0-9A-Za-z-]*)(?:\.(?:0|[1-9A-Za-z-][0-9A-Za-z-]*))*))?"
    r"(?:\+((?:0|[1-9A-Za-z-][0-9A-Za-z-]*)(?:\.(?:0|[1-9A-Za-z-][0-9A-Za-z-]*))*))?$"
)
PROPERTY = re.compile(r"^\s*([A-Za-z0-9_.-]+)\s*=\s*(.*?)\s*$")
TOML_VALUE = re.compile(r'^\s*([A-Za-z0-9_.-]+)\s*=\s*"([^"]*)"\s*$')


class VerificationError(Exception):
    """Raised when an installable artifact fails a release check."""


def read_properties(path: Path) -> dict[str, str]:
    properties: dict[str, str] = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        stripped = line.strip()
        if not stripped or stripped.startswith(("#", "!")):
            continue
        match = PROPERTY.match(line)
        if match:
            properties[match.group(1)] = match.group(2)
    return properties


def read_mod_metadata(archive: zipfile.ZipFile) -> dict[str, str]:
    try:
        text = archive.read("META-INF/mods.toml").decode("utf-8")
    except KeyError as exc:
        raise VerificationError("JAR is missing META-INF/mods.toml") from exc

    metadata: dict[str, str] = {}
    for line in text.splitlines():
        match = TOML_VALUE.match(line)
        if match and match.group(1) in {"version", "license"}:
            metadata[match.group(1)] = match.group(2)
    return metadata


def verify(root: Path) -> tuple[Path, str]:
    properties_path = root / "gradle.properties"
    if not properties_path.is_file():
        raise VerificationError("gradle.properties was not found")

    properties = read_properties(properties_path)
    mod_id = properties.get("mod_id", "")
    version = properties.get("mod_version", "")
    license_name = properties.get("mod_license", "")
    if not mod_id or not version:
        raise VerificationError("gradle.properties must define mod_id and mod_version")
    if not SEMVER.fullmatch(version):
        raise VerificationError(f"mod_version is not valid SemVer: {version!r}")
    if license_name != "WTFPL":
        raise VerificationError(f"gradle.properties license must be WTFPL, got {license_name!r}")

    artifact = root / "build" / "libs" / f"{mod_id}-{version}-all.jar"
    if not artifact.is_file():
        raise VerificationError(f"installable artifact not found: {artifact}")
    if not artifact.name.endswith("-all.jar"):
        raise VerificationError(f"release artifact is not an -all.jar: {artifact.name}")

    with zipfile.ZipFile(artifact) as archive:
        names = set(archive.namelist())
        metadata = read_mod_metadata(archive)
        if metadata.get("version") != version:
            raise VerificationError(
                f"embedded mod version mismatch: expected {version!r}, got {metadata.get('version')!r}"
            )
        if metadata.get("license") != license_name:
            raise VerificationError(
                f"embedded license mismatch: expected {license_name!r}, got {metadata.get('license')!r}"
            )
        required = {
            "META-INF/mods.toml",
            "META-INF/jarjar/metadata.json",
            "META-INF/jarjar/nv-websocket-client-2.14.jar",
            f"assets/{mod_id}/sounds.json",
            "META-INF/LICENSE-bddmod.txt",
            "META-INF/THIRD_PARTY_LICENSES-bddmod.txt",
        }
        missing = sorted(required - names)
        if missing:
            raise VerificationError("JAR is missing required entries: " + ", ".join(missing))

    digest = hashlib.sha256(artifact.read_bytes()).hexdigest().upper()
    print(f"artifact={artifact.relative_to(root).as_posix()}")
    print(f"version={version}")
    print(f"license={license_name}")
    print(f"sha256={digest}")
    return artifact, digest


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    args = parser.parse_args()
    try:
        verify(args.root.resolve())
    except (OSError, zipfile.BadZipFile, VerificationError) as exc:
        print(f"verify_build: FAIL: {exc}", file=sys.stderr)
        return 1
    print("verify_build: PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
