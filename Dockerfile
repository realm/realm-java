FROM ubuntu:16.04

# Locales
RUN locale-gen en_US.UTF-8
ENV LANG "en_US.UTF-8"
ENV LANGUAGE "en_US.UTF-8"
ENV LC_ALL "en_US.UTF-8"

# Set the environment variables
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV ANDROID_HOME /opt/android-sdk-linux
ENV NDK_HOME /opt/android-ndk
# Need by cmake
ENV ANDROID_NDK_HOME /opt/android-ndk
ENV PATH ${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools
ENV PATH ${PATH}:${NDK_HOME}

# Install the JDK
# We are going to need some 32 bit binaries because aapt requires it
# file is need by the script that creates NDK toolchains
RUN DEBIAN_FRONTEND=noninteractive dpkg --add-architecture i386 \
    && apt-get update -qq \
    && apt-get install -y file git curl wget zip unzip \
                       build-essential \
                       openjdk-8-jdk-headless \
                       libc6:i386 libstdc++6:i386 libgcc1:i386 libncurses5:i386 libz1:i386 \
    && apt-get clean

# Install the Android SDK
RUN cd /opt && wget -q https://dl.google.com/android/repository/tools_r25.1.7-linux.zip -O android-tools-linux.zip
RUN cd /opt && unzip android-tools-linux.zip -d ${ANDROID_HOME}
RUN cd /opt && rm -f android-tools-linux.zip

# Grab what's needed in the SDK
# ↓ updates tools to at least 25.1.7, but that prints 'Nothing was installed' (so I don't check the outputs).
RUN echo y | android update sdk --no-ui --all --filter tools > /dev/null 
RUN echo y | android update sdk --no-ui --all --filter platform-tools | grep 'package installed'
RUN echo y | android update sdk --no-ui --all --filter build-tools-24.0.0 | grep 'package installed'
RUN echo y | android update sdk --no-ui --all --filter extra-android-m2repository | grep 'package installed'
RUN echo y | android update sdk --no-ui --all --filter android-24 | grep 'package installed'

# Install the NDK
RUN mkdir /opt/android-ndk-tmp
RUN cd /opt/android-ndk-tmp && wget -q http://dl.google.com/android/ndk/android-ndk-r10e-linux-x86_64.bin -O android-ndk.bin
RUN cd /opt/android-ndk-tmp && chmod a+x ./android-ndk.bin && ./android-ndk.bin
RUN cd /opt/android-ndk-tmp && mv ./android-ndk-r10e /opt/android-ndk
RUN rm -rf /opt/android-ndk-tmp

# Install cmake
RUN cd /opt && wget -q https://dl.google.com/android/repository/cmake-3.4.2909474-linux-x86_64.zip -O cmake-linux.zip
RUN cd /opt && unzip cmake-linux.zip -d ${ANDROID_HOME}/cmake
RUN cd /opt && rm -f cmake-linux.zip

