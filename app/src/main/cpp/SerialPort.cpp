#include <jni.h>
#include <string>
#include <fcntl.h>
#include <unistd.h>
#include <termios.h>
#include <android/log.h>

#define TAG "SerialPortJNI"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)

/**
 * Helper function to create a java.io.FileDescriptor object from a native file descriptor integer.
 * This function uses reflection to set the private 'descriptor' field.
 * @param env JNI Environment
 * @param fd The native integer file descriptor
 * @return A new jobject representing the FileDescriptor, or nullptr on failure.
 */
static jobject jni_create_file_descriptor(JNIEnv *env, int fd) {
    if (fd < 0) return nullptr;

    jclass fileDescriptorClass = env->FindClass("java/io/FileDescriptor");
    if (fileDescriptorClass == nullptr) {
        LOGE("Could not find class java/io/FileDescriptor");
        return nullptr;
    }

    jmethodID constructor = env->GetMethodID(fileDescriptorClass, "<init>", "()V");
    if (constructor == nullptr) {
        LOGE("Could not find constructor for java/io/FileDescriptor");
        return nullptr;
    }

    jobject fileDescriptor = env->NewObject(fileDescriptorClass, constructor);

    // Use reflection to set the private 'descriptor' field
    jfieldID descriptorField = env->GetFieldID(fileDescriptorClass, "descriptor", "I");
    if (descriptorField == nullptr) {
        LOGE("Could not find field 'descriptor' in java/io/FileDescriptor");
        return nullptr;
    }

    env->SetIntField(fileDescriptor, descriptorField, (jint) fd);

    return fileDescriptor;
}

/**
 * Helper function to get the native file descriptor integer from a java.io.FileDescriptor object.
 * This function uses reflection to get the private 'descriptor' field.
 * @param env JNI Environment
 * @param fileDescriptor The jobject representing the FileDescriptor
 * @return The native integer file descriptor, or -1 on failure.
 */
static int jni_get_file_descriptor(JNIEnv *env, jobject fileDescriptor) {
    if (fileDescriptor == nullptr) return -1;

    jclass fileDescriptorClass = env->GetObjectClass(fileDescriptor);
    if (fileDescriptorClass == nullptr) {
        LOGE("Could not get class for FileDescriptor object");
        return -1;
    }

    jfieldID descriptorField = env->GetFieldID(fileDescriptorClass, "descriptor", "I");
    if (descriptorField == nullptr) {
        LOGE("Could not find field 'descriptor' in java/io/FileDescriptor for get");
        return -1;
    }

    return env->GetIntField(fileDescriptor, descriptorField);
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_thanes_wardstock_services_usb_SerialPortJNI_openPort(
        JNIEnv *env,
        jobject, /* this */
        jstring devicePath_,
        jint baudRate) {

    const char *devicePath = env->GetStringUTFChars(devicePath_, nullptr);

    int fd = open(devicePath, O_RDWR | O_NOCTTY);
    if (fd < 0) {
        LOGE("Failed to open device: %s", devicePath);
        env->ReleaseStringUTFChars(devicePath_, devicePath);
        return nullptr;
    }

    struct termios options{};
    if (tcgetattr(fd, &options) != 0) {
        LOGE("tcgetattr failed for %s", devicePath);
        close(fd);
        env->ReleaseStringUTFChars(devicePath_, devicePath);
        return nullptr;
    }

    // Set baud rate
    speed_t speed;
    switch (baudRate) {
        case 9600:
            speed = B9600;
            break;
        case 19200:
            speed = B19200;
            break;
        case 38400:
            speed = B38400;
            break;
        case 57600:
            speed = B57600;
            break;
        case 115200:
            speed = B115200;
            break;
        default:
            LOGE("Unsupported baud rate: %d for device %s", baudRate, devicePath);
            close(fd);
            env->ReleaseStringUTFChars(devicePath_, devicePath);
            return nullptr;
    }
    cfsetispeed(&options, speed);
    cfsetospeed(&options, speed);

    // Set serial port parameters for raw communication
    options.c_cflag &= ~CSIZE;
    options.c_cflag |= CS8;      // 8 data bits
    options.c_cflag &= ~PARENB;  // No parity
    options.c_cflag &= ~CSTOPB;  // 1 stop bit
    options.c_cflag |= CREAD | CLOCAL; // Enable receiver, ignore control lines
    options.c_iflag &= ~(IXON | IXOFF | IXANY); // turn off s/w flow ctrl
    options.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG); // make raw
    options.c_oflag &= ~OPOST; // make raw

    // Set read timeouts (VMIN and VTIME)
    options.c_cc[VMIN] = 0; // Read will return immediately, even if no data is available
    options.c_cc[VTIME] = 5; // Timeout in deciseconds (0.5 seconds)

    // Apply the settings
    tcflush(fd, TCIFLUSH);
    if (tcsetattr(fd, TCSANOW, &options) != 0) {
        LOGE("tcsetattr failed for %s", devicePath);
        close(fd);
        env->ReleaseStringUTFChars(devicePath_, devicePath);
        return nullptr;
    }

    LOGI("Successfully opened and configured: %s with fd=%d", devicePath, fd);
    env->ReleaseStringUTFChars(devicePath_, devicePath);

    // Create and return the Java FileDescriptor object
    return jni_create_file_descriptor(env, fd);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_thanes_wardstock_services_usb_SerialPortJNI_closePortFromFileDescriptor(
        JNIEnv *env,
        jobject, /* this */
        jobject fileDescriptor) {

    int fd = jni_get_file_descriptor(env, fileDescriptor);
    if (fd >= 0) {
        close(fd);
        LOGI("Port with fd=%d closed.", fd);
    } else {
        LOGW("Attempted to close an invalid file descriptor.");
    }
}
