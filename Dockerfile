FROM ubuntu:16.04

# Locales
RUN locale-gen en_US.UTF-8
ENV LANG "en_US.UTF-8"
ENV LANGUAGE "en_US.UTF-8"
ENV LC_ALL "en_US.UTF-8"

# Set the environment variables
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV ANDROID_HOME /tmp/opt/android-sdk-linux
ENV NDK_HOME /tmp/opt/android-ndk
ENV PATH ${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools
ENV PATH ${PATH}:${NDK_HOME}

# Install the JDK
# We are going to need some 32 bit binaries because aapt requires it
# file is need by the script that creates NDK toolchains
RUN DEBIAN_FRONTEND=noninteractive dpkg --add-architecture i386 \
    && apt-get update -qq \
    && apt-get install -y file git curl wget zip unzip \
                       bsdmainutils \
                       build-essential \
                       openjdk-8-jdk-headless \
                       libc6:i386 libstdc++6:i386 libgcc1:i386 libncurses5:i386 libz1:i386 \
                       s3cmd nodejs libconfig++9v5\
    && apt-get clean

# Install writable dir
RUN mkdir /tmp/opt && chmod 777 /tmp/opt

# Install the Android SDK
RUN cd /tmp/opt && \
    wget -q https://dl.google.com/android/repository/tools_r25.1.7-linux.zip -O android-tools-linux.zip && \
    unzip android-tools-linux.zip -d ${ANDROID_HOME} && \
    rm -f android-tools-linux.zip

# Grab what's needed in the SDK
# ↓ updates tools to at least 25.1.7, but that prints 'Nothing was installed' (so I don't check the outputs).
RUN echo y | android update sdk --no-ui --all --filter tools > /dev/null
RUN echo y | android update sdk --no-ui --all --filter platform-tools | grep 'package installed'
RUN echo y | android update sdk --no-ui --all --filter build-tools-24.0.0 | grep 'package installed'
RUN echo y | android update sdk --no-ui --all --filter extra-android-m2repository | grep 'package installed'
RUN echo y | android update sdk --no-ui --all --filter android-24 | grep 'package installed'

# Install the NDK
RUN mkdir /tmp/opt/android-ndk-tmp && \
    cd /tmp/opt/android-ndk-tmp && \
    wget -q http://dl.google.com/android/ndk/android-ndk-r10e-linux-x86_64.bin -O android-ndk.bin && \
    chmod a+x ./android-ndk.bin && \
    ./android-ndk.bin && \
    mv android-ndk-r10e /opt/android-ndk && \
    rm -rf /tmp/opt/android-ndk-tmp && \
    chmod -R a+rX /tmp/opt/android-ndk

# Install cmake
RUN mkdir /tmp/opt/cmake-tmp && \
    cd /tmp/opt/cmake-tmp && \
    wget -q https://dl.google.com/android/repository/cmake-3.6.3133135-linux-x86_64.zip -O cmake-linux.zip && \
    unzip cmake-linux.zip -d ${ANDROID_HOME}/cmake && \
    rm -rf /tmp/opt/cmake-tmp

# Make the SDK and NDK universally readable
RUN chmod -R a+rX ${ANDROID_HOME} && chmod -R a+rX ${NDK_HOME}
