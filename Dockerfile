FROM ubuntu:16.04

# Locales
RUN locale-gen en_US.UTF-8
ENV LANG "en_US.UTF-8"
ENV LANGUAGE "en_US.UTF-8"
ENV LC_ALL "en_US.UTF-8"

# Set the environment variables
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV ANDROID_HOME /opt/android-sdk-linux
# Need by cmake
ENV ANDROID_NDK_HOME /opt/android-ndk
ENV ANDROID_NDK /opt/android-ndk
ENV PATH ${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/tools/bin:${ANDROID_HOME}/platform-tools
ENV PATH ${PATH}:${NDK_HOME}
ENV NDK_CCACHE /usr/bin/ccache
ENV NDK_LCACHE /usr/bin/lcache

# The 32 bit binaries because aapt requires it
# `file` is need by the script that creates NDK toolchains
# Keep the packages in alphabetical order to make it easy to avoid duplication
RUN DEBIAN_FRONTEND=noninteractive dpkg --add-architecture i386 \
    && apt-get update -qq \
    && apt-get install -y bsdmainutils \
                          build-essential \
                          ccache \
                          curl \
                          file \
                          git \
                          libc6:i386 \
                          libgcc1:i386 \
                          libncurses5:i386 \
                          libstdc++6:i386 \
                          libz1:i386 \
                          openjdk-8-jdk-headless \
                          s3cmd \
                          unzip \
                          wget \
                          zip \
    && apt-get clean

# Install the Android SDK
RUN cd /opt && \
    wget -q https://dl.google.com/android/repository/sdk-tools-linux-3859397.zip -O android-tools-linux.zip && \
    unzip android-tools-linux.zip -d ${ANDROID_HOME} && \
    rm -f android-tools-linux.zip

# Grab what's needed in the SDK
RUN mkdir "${ANDROID_HOME}/licenses" && \
    echo -e "\n8933bad161af4178b1185d1a37fbf41ea5269c55" > "${ANDROID_HOME}/licenses/android-sdk-license"
RUN sdkmanager --update
RUN sdkmanager 'platform-tools'
RUN sdkmanager 'build-tools;26.0.0'
RUN sdkmanager 'extras;android;m2repository'
RUN sdkmanager 'platforms;android-25'

# Install the NDK
RUN mkdir /opt/android-ndk-tmp && \
    cd /opt/android-ndk-tmp && \
    wget -q http://dl.google.com/android/ndk/android-ndk-r10e-linux-x86_64.bin -O android-ndk.bin && \
    chmod a+x ./android-ndk.bin && \
    ./android-ndk.bin && \
    mv android-ndk-r10e /opt/android-ndk && \
    rm -rf /opt/android-ndk-tmp && \
    chmod -R a+rX /opt/android-ndk

# Install cmake
RUN mkdir /opt/cmake-tmp && \
    cd /opt/cmake-tmp && \
    wget -q https://dl.google.com/android/repository/cmake-3.6.3155560-linux-x86_64.zip -O cmake-linux.zip && \
    mkdir -p ${ANDROID_HOME}/cmake/3.6.3155560 && \
    unzip cmake-linux.zip -d ${ANDROID_HOME}/cmake/3.6.3155560 && \
    rm -rf /opt/cmake-tmp

# Make the SDK universally writable
RUN chmod -R a+rwX ${ANDROID_HOME}

# Install lcache
RUN wget -q https://github.com/beeender/lcache/releases/download/v0.0.2/lcache-linux -O /usr/bin/lcache && \
    chmod +x /usr/bin/lcache
