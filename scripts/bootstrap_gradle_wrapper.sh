#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")"/.. && pwd)"
WRAPPER_JAR="${ROOT_DIR}/gradle/wrapper/gradle-wrapper.jar"
WRAPPER_PROPS="${ROOT_DIR}/gradle/wrapper/gradle-wrapper.properties"

if [[ -f "${WRAPPER_JAR}" ]]; then
  echo "gradle-wrapper.jar already present"
  exit 0
fi

if [[ ! -f "${WRAPPER_PROPS}" ]]; then
  echo "Unable to locate gradle-wrapper.properties at ${WRAPPER_PROPS}" >&2
  exit 1
fi

if ! command -v curl >/dev/null 2>&1; then
  echo "curl is required to download gradle-wrapper.jar" >&2
  exit 1
fi

if ! command -v unzip >/dev/null 2>&1; then
  echo "unzip is required to extract gradle-wrapper.jar" >&2
  exit 1
fi

if ! command -v jar >/dev/null 2>&1; then
  echo "jar (from the JDK) is required to build gradle-wrapper.jar" >&2
  exit 1
fi

DIST_URL_RAW=$(grep '^distributionUrl=' "${WRAPPER_PROPS}" | cut -d'=' -f2-)
DIST_URL=${DIST_URL_RAW//\\/}

VERSION=$(echo "${DIST_URL}" | sed -E 's#.*/gradle-([0-9]+(\.[0-9]+)*).*#\1#')
if [[ -z "${VERSION}" ]]; then
  echo "Unable to parse Gradle version from distributionUrl=${DIST_URL_RAW}" >&2
  exit 1
fi

DOWNLOAD_URL="${DIST_URL}"

echo "Downloading Gradle distribution from ${DOWNLOAD_URL}..."
TMP_ZIP=$(mktemp)
TMP_FILE="${WRAPPER_JAR}.tmp"
rm -f "${TMP_FILE}"

cleanup() {
  rm -f "${TMP_ZIP}" "${TMP_FILE}" "${TMP_WRAPPER_JAR}" "${TMP_SHARED_JAR}" "${TMP_DEPENDENCY_JARS[@]-}"
  rm -rf "${TMP_DIR}" "${TMP_EXTRACT_DIR}"
}
trap cleanup EXIT

if ! curl -fL "${DOWNLOAD_URL}" -o "${TMP_ZIP}"; then
  echo "Failed to download Gradle distribution from ${DOWNLOAD_URL}" >&2
  exit 1
fi

ARCHIVE_ROOT="gradle-${VERSION}"
PLUGIN_JAR_PATH="${ARCHIVE_ROOT}/lib/plugins/gradle-wrapper-${VERSION}.jar"
SHARED_JAR_PATH="${ARCHIVE_ROOT}/lib/gradle-wrapper-shared-${VERSION}.jar"
DEPENDENCY_JARS=(
  "${ARCHIVE_ROOT}/lib/gradle-base-annotations-${VERSION}.jar"
  "${ARCHIVE_ROOT}/lib/gradle-cli-${VERSION}.jar"
  "${ARCHIVE_ROOT}/lib/gradle-files-${VERSION}.jar"
  "${ARCHIVE_ROOT}/lib/gradle-functional-${VERSION}.jar"
)

TMP_DIR=$(mktemp -d)
TMP_EXTRACT_DIR=$(mktemp -d)
TMP_WRAPPER_JAR="${TMP_DIR}/gradle-wrapper-${VERSION}.jar"
TMP_SHARED_JAR="${TMP_DIR}/gradle-wrapper-shared-${VERSION}.jar"
TMP_DEPENDENCY_JARS=()

if ! unzip -p "${TMP_ZIP}" "${PLUGIN_JAR_PATH}" > "${TMP_WRAPPER_JAR}"; then
  echo "Failed to extract ${PLUGIN_JAR_PATH} from ${DOWNLOAD_URL}" >&2
  exit 1
fi

if ! unzip -p "${TMP_ZIP}" "${SHARED_JAR_PATH}" > "${TMP_SHARED_JAR}"; then
  echo "Failed to extract ${SHARED_JAR_PATH} from ${DOWNLOAD_URL}" >&2
  exit 1
fi

(cd "${TMP_EXTRACT_DIR}" && jar xf "${TMP_WRAPPER_JAR}")
(cd "${TMP_EXTRACT_DIR}" && jar xf "${TMP_SHARED_JAR}")

for DEP_PATH in "${DEPENDENCY_JARS[@]}"; do
  TMP_DEP_JAR="${TMP_DIR}/$(basename "${DEP_PATH}")"
  TMP_DEPENDENCY_JARS+=("${TMP_DEP_JAR}")
  if ! unzip -p "${TMP_ZIP}" "${DEP_PATH}" > "${TMP_DEP_JAR}"; then
    echo "Failed to extract ${DEP_PATH} from ${DOWNLOAD_URL}" >&2
    exit 1
  fi
  (cd "${TMP_EXTRACT_DIR}" && jar xf "${TMP_DEP_JAR}")
done

MANIFEST_FILE="${TMP_DIR}/MANIFEST.MF"
cat <<'EOF' > "${MANIFEST_FILE}"
Manifest-Version: 1.0
Main-Class: org.gradle.wrapper.GradleWrapperMain
EOF

(cd "${TMP_EXTRACT_DIR}" && jar cfm "${TMP_FILE}" "${MANIFEST_FILE}" .)

mv "${TMP_FILE}" "${WRAPPER_JAR}"
trap - EXIT
cleanup

echo "gradle-wrapper.jar downloaded to ${WRAPPER_JAR}"
