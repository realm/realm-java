FROM ubuntu:18.04

# Locales
RUN apt-get clean && apt-get -y update && apt-get install -y locales && locale-gen en_US.UTF-8
ENV LANG "en_US.UTF-8"
ENV LANGUAGE "en_US.UTF-8"
ENV LC_ALL "en_US.UTF-8"
ENV TZ=Europe/Copenhagen
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Set the environment variables
ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk-amd64
ENV JAVA8_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV ANDROID_HOME /opt/android-sdk-linux
# Need by cmake
ENV ANDROID_NDK_HOME /opt/android-ndk
ENV ANDROID_NDK /opt/android-ndk
ENV PATH ${PATH}:${ANDROID_HOME}/emulator:${ANDROID_HOME}/cmdline-tools/latest:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools
ENV PATH ${PATH}:${NDK_HOME}
ENV NDK_CCACHE /usr/bin/ccache
ENV CCACHE_CPP2 yes
ENV REALM_DISABLE_ANALYTICS true

# Keep the packages in alphabetical order to make it easy to avoid duplication
# tzdata needs to be installed first. See https://askubuntu.com/questions/909277/avoiding-user-interaction-with-tzdata-when-installing-certbot-in-a-docker-contai
# `file` is need by the Android Emulator
RUN DEBIAN_FRONTEND=noninteractive \
    && apt-get update -qq \
    && apt-get install -y tzdata \
    && apt-get install -y bsdmainutils \
                          bridge-utils \
                          build-essential \
                          ccache \
                          curl \
                          file \
                          git \
                          jq \
                          libc6 \
                          libgcc1 \
                          libglu1 \
                          libncurses5 \
                          libstdc++6 \
                          libz1 \
                          libvirt-clients \
                          libvirt-daemon-system \
                          openjdk-11-jdk-headless \
                          openjdk-8-jdk-headless \
                          qemu-kvm \
                          s3cmd \
                          unzip \
                          virt-manager \
                          wget \
                          zip \
                          ninja-build \
    && apt-get clean

# Install the Android SDK
# See https://developer.android.com/studio/index.html#downloads for latest version
RUN cd /opt && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-7302050_latest.zip -O android-tools-linux.zip && \
    mkdir --parents ${ANDROID_HOME}/cmdline-tools/latest && \
    unzip android-tools-linux.zip -d ${ANDROID_HOME}/cmdline-tools/latest && \
    mv ${ANDROID_HOME}/cmdline-tools/latest/cmdline-tools/* ${ANDROID_HOME}/cmdline-tools/latest/ && \
    rm -f android-tools-linux.zip

# Grab what's needed in the SDK
RUN sdkmanager --update

# Accept licenses before installing components, no need to echo y for each component
# License is valid for all the standard components in versions installed from this file
# Non-standard components: MIPS system images, preview versions, GDK (Google Glass) and Android Google TV require separate licenses, not accepted there
RUN yes | sdkmanager --licenses

# SDKs
# The `yes` is for accepting all non-standard tool licenses.
# Please keep all sections in descending order!
RUN yes | sdkmanager \
    'build-tools;30.0.3' \
    'emulator' \
    'extras;android;m2repository' \
    'platforms;android-30' \
    'platform-tools' \
    'ndk;23.1.7779620' \
    'system-images;android-29;default;x86'

# Make the SDK universally writable
RUN chmod -R a+rwX ${ANDROID_HOME}

# Ensure a new enough version of CMake is available.
RUN cd /opt \
    && wget -nv https://cmake.org/files/v3.21/cmake-3.21.4-linux-x86_64.tar.gz \
    && tar zxf cmake-3.21.4-linux-x86_64.tar.gz

# Workaround for https://issuetracker.google.com/issues/206099937
RUN ln -s /usr/bin/ninja /opt/cmake-3.21.4-linux-x86_64/bin/ninja

ENV PATH "/opt/cmake-3.21.4-linux-x86_64/bin:$PATH"
