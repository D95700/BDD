#!/usr/bin/env python3
"""Verify every installable JAR intended for a stable release."""

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
    """Raised when one release asset fails a packaging check."""


def read_properties(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        stripped = line.strip()
        if not stripped or stripped.startswith(("#", "!")):
            continue
        match = PROPERTY.match(line)
        if match:
            values[match.group(1)] = match.group(2)
    return values


def read_metadata(archive: zipfile.ZipFile) -> dict[str, str]:
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


def verify_artifact(path: Path, expected_version: str, mod_id: str, license_name: str) -> str:
    if not SEMVER.fullmatch(expected_version):
        raise VerificationError(f"invalid expected SemVer: {expected_version!r}")
    expected_name = f"{mod_id}-{expected_version}-all.jar"
    if path.name != expected_name:
        raise VerificationError(f"expected asset name {expected_name!r}, got {path.name!r}")
    if not path.is_file():
        raise VerificationError(f"asset not found: {path}")

    required = {
        "META-INF/mods.toml",
        "META-INF/jarjar/metadata.json",
        "META-INF/jarjar/nv-websocket-client-2.14.jar",
        f"assets/{mod_id}/sounds.json",
        "META-INF/LICENSE-bddmod.txt",
        "META-INF/THIRD_PARTY_LICENSES-bddmod.txt",
    }
    try:
        with zipfile.ZipFile(path) as archive:
            names = set(archive.namelist())
            metadata = read_metadata(archive)
            missing = sorted(required - names)
            if missing:
                raise VerificationError(f"{path.name} is missing: {', '.join(missing)}")
            if metadata.get("version") != expected_version:
                raise VerificationError(
                    f"{path.name} embeds version {metadata.get('version')!r}, expected {expected_version!r}"
                )
            if metadata.get("license") != license_name:
                raise VerificationError(
                    f"{path.name} embeds license {metadata.get('license')!r}, expected {license_name!r}"
                )
    except (OSError, zipfile.BadZipFile) as exc:
        raise VerificationError(f"could not read {path}: {exc}") from exc

    return hashlib.sha256(path.read_bytes()).hexdigest().upper()


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument(
        "--artifact",
        action="append",
        required=True,
        metavar="VERSION=PATH",
        help="artifact mapping; repeat once for every prerelease and stable JAR",
    )
    args = parser.parse_args()
    root = args.root.resolve()
    properties = read_properties(root / "gradle.properties")
    mod_id = properties.get("mod_id", "")
    license_name = properties.get("mod_license", "")
    if not mod_id or license_name != "WTFPL":
        print("verify_release_assets: FAIL: gradle.properties must define mod_id and WTFPL", file=sys.stderr)
        return 1

    seen_versions: set[str] = set()
    try:
        for mapping in args.artifact:
            if "=" not in mapping:
                raise VerificationError(f"artifact mapping must use VERSION=PATH: {mapping!r}")
            version, raw_path = mapping.split("=", 1)
            if version in seen_versions:
                raise VerificationError(f"duplicate release version: {version}")
            seen_versions.add(version)
            path = Path(raw_path)
            if not path.is_absolute():
                path = root / path
            digest = verify_artifact(path.resolve(), version, mod_id, license_name)
            print(f"version={version} artifact={path.resolve().relative_to(root).as_posix()} sha256={digest}")
    except (OSError, VerificationError) as exc:
        print(f"verify_release_assets: FAIL: {exc}", file=sys.stderr)
        return 1

    if not seen_versions:
        print("verify_release_assets: FAIL: no artifacts supplied", file=sys.stderr)
        return 1
    print(f"verify_release_assets: PASS ({len(seen_versions)} artifacts)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
