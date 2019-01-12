#ifdef __cplusplus
extern "C" {
#endif
#include <android/log.h>

#define  LOG_TAG    "ffmpeg"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

typedef struct PictureJ {
	int linesize;
	void *buffer;
} PictureJ;

int ffmpeg_test();
int convert_init();
int ffmpeg_mjpeg2rgb(void *inputBuf, void *outBuf, int bufSize);
int ffmpeg_yuv2rgb(void *inputBuf, void *outBuf, int bufSize);
void processimage (const void *p, void *outbuf,int l);

#ifdef __cplusplus
}
#endif