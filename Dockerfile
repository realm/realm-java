FROM ubuntu:16.04

# Locales
RUN apt-get clean && apt-get -y update && apt-get install -y locales && locale-gen en_US.UTF-8
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
RUN sdkmanager --update

# Accept licenses before installing components, no need to echo y for each component
# License is valid for all the standard components in versions installed from this file
# Non-standard components: MIPS system images, preview versions, GDK (Google Glass) and Android Google TV require separate licenses, not accepted there
RUN yes | sdkmanager --licenses

# SDKs
# Please keep these in descending order!
# The `yes` is for accepting all non-standard tool licenses.
# Please keep all sections in descending order!
RUN yes | sdkmanager \
    'platform-tools' \
    'build-tools;28.0.3' \
    'extras;android;m2repository' \
    'platforms;android-27' \
    'cmake;3.6.4111459'

# Install the NDK
RUN mkdir /opt/android-ndk-tmp && \
    cd /opt/android-ndk-tmp && \
    wget -q http://dl.google.com/android/ndk/android-ndk-r10e-linux-x86_64.bin -O android-ndk.bin && \
    chmod a+x ./android-ndk.bin && \
    ./android-ndk.bin && \
    mv android-ndk-r10e /opt/android-ndk && \
    rm -rf /opt/android-ndk-tmp && \
    chmod -R a+rX /opt/android-ndk && \
    echo "Pkg.Desc = Android NDK\nPkg.Revision = 10.0.0" > /opt/android-ndk/source.properties

# Make the SDK universally writable
RUN chmod -R a+rwX ${ANDROID_HOME}
