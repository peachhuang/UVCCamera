#include <stdio.h>
#include <libavcodec/avcodec.h>
#include <libavutil/opt.h>
#include <libavutil/pixfmt.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include "ffmpeg_opt.h"
#include "ffjpeg.h"

AVCodecContext * _transcoder = NULL;
AVFrame * _outputFrame, *pFrameYUV;
AVPacket _inputPacket;
struct SwsContext *mJPEGconvertCtx;
struct SwsContext *mJPEGconvertCtx1;
int width = 1280,height = 720;
int size;

enum AVPixelFormat sws_src_fmt;
enum AVPixelFormat sws_dst_fmt = AV_PIX_FMT_YUV420P;//AV_PIX_FMT_RGBA;//AV_PIX_FMT_RGBA;//;AV_PIX_FMT_YUYV422

typedef struct {
    AVCodec        *codec;
    AVCodecContext *ctxt;
    AVFrame         frame;
} JPEGDEC;

JPEGDEC *jpegdec;

int convert_init(){

    jpegdec = ffjpeg_decoder_init();
    if(jpegdec == NULL) LOGE("ffjpeg_decoder_init failed.");

    mJPEGconvertCtx1 = sws_getContext(1280, 720, AV_PIX_FMT_YUVJ420P,
            1280, 720, sws_dst_fmt, SWS_BILINEAR, NULL, NULL, NULL);
/*    avcodec_register_all();
    _outputFrame = av_frame_alloc();
    av_frame_unref(_outputFrame);
    av_init_packet(&_inputPacket);

    AVCodec * codecDecode = avcodec_find_decoder(AV_CODEC_ID_MJPEG);

    _transcoder = avcodec_alloc_context3(codecDecode);
    avcodec_get_context_defaults3(_transcoder, codecDecode);
    _transcoder->flags2 |= CODEC_FLAG2_FAST;
    _transcoder->pix_fmt = AV_PIX_FMT_YUVJ420P;//AV_PIX_FMT_RGB24;//AVPixelFormat::AV_PIX_FMT_YUV420P;
    _transcoder->width = width;
    _transcoder->height = height;
    avcodec_open2(_transcoder, codecDecode, NULL);

    // swscale contex init
    mJPEGconvertCtx = sws_getContext(width, height, AV_PIX_FMT_YUVJ422P,
            width, height, AV_PIX_FMT_YUV420P, SWS_FAST_BILINEAR, NULL, NULL, NULL);

    pFrameYUV=avcodec_alloc_frame();*/
    LOGI("convert_init Done.");
}
int ffmpeg_mjpeg2rgb(void *srcbuf, void *dstbuf, int bufSize)
{
    int i;
    int dstlen, /*dstfmt = HAL_PIXEL_FORMAT_RGBX_8888,*/ dstw = 1280, dsth = 720;
    int srclen = bufSize, /*srcfmt = V4L2_PIX_FMT_MJPEG,*/ srcw = 1280, srch = 720, pts;
	char *picOut;

    char *tmp = srcbuf;
	//ffmpeg_yuv2rgb(srcbuf, dstbuf, bufSize);
    for(i=0;i<100;){
        LOGI("[%d] %d %d %d %d %d %d %d %d %d %d",i,(char)tmp[i],(char)tmp[i+1],
            tmp[i+2],tmp[i+3],tmp[i+4],tmp[i+5],tmp[i+6],tmp[i+7],tmp[i+8],tmp[i+9]);
        i=i+10;
    }
					FILE *fp = fopen("/sdcard/DCIM/ffmpeg/mjpeg.jpg", "w+");
					if (fp != NULL) {
						fwrite(srcbuf, bufSize, 1, fp);	
						fclose(fp); 
					} else
						LOGE("mjpeg.jpg fail!"); 

	AVFrame *frame = av_frame_alloc();
	frame->format = sws_dst_fmt;
    frame->width = srcw;
    frame->height = srch;

	int numBytes = av_image_get_buffer_size(sws_dst_fmt, width, height, 1);
	LOGI("ffmpeg_mjpeg2rgb 04-1. numBytes %d",numBytes);
	uint8_t *pbuffer = (uint8_t *) av_malloc(numBytes * sizeof(uint8_t));
	LOGI("ffmpeg_mjpeg2rgb 04-2.");
	av_image_fill_arrays(frame->data, frame->linesize,
	         pbuffer,
	         sws_dst_fmt,
	         srcw,
	         srch, 1);
	
	LOGI("ffmpeg_mjpeg2rgb 01.");
	LOGI("ffmpeg_mjpeg2rgb 02. srclen= %d",srclen);
	AVFrame *pic = ffjpeg_decoder_decode(jpegdec, srcbuf, srclen);

	LOGI("ffmpeg_mjpeg2rgb 03. sws_src_fmt=%d pic->format=%d",
	    sws_src_fmt,pic->format);
        
    tmp = pic->data[0];
    for(i=0;i<100;){
        LOGI("[%d] %d %d %d %d %d %d %d %d %d %d",i,(char)tmp[i],(char)tmp[i+1],
            tmp[i+2],tmp[i+3],tmp[i+4],tmp[i+5],tmp[i+6],tmp[i+7],tmp[i+8],tmp[i+9]);
        i=i+10;
    }

					fp = fopen("/sdcard/DCIM/ffmpeg/decode.jpg", "w+");
					if (fp != NULL) {
						fwrite(pic->data, av_image_get_buffer_size(AV_PIX_FMT_YUVJ420P, srcw, srch, 1), 1, fp);	
						fclose(fp); 
					} else
						LOGE("decode.jpg fail!"); 
	LOGI("ffmpeg_mjpeg2rgb 04.");

	LOGI("ffmpeg_mjpeg2rgb 05.pic->linesize=%d",pic->linesize[0]);
    if(mJPEGconvertCtx1 ==NULL || pic->data ==NULL ||  frame->data==NULL)
        LOGI("some is NULL");
  //  sws_scale(mJPEGconvertCtx1, src_data, src_linesize, 0, srch, dst_data, dst_linesize);
    sws_scale(mJPEGconvertCtx1, pic->data, pic->linesize, 0, srch, frame->data, frame->linesize);

  					fp = fopen("/sdcard/DCIM/ffmpeg/scale.jpg", "w+");
					if (fp != NULL) {
						fwrite(frame->data, av_image_get_buffer_size(sws_dst_fmt, srcw, srch, 1), 1, fp);	
						fclose(fp); 
					} else
						LOGE("scale.jpg fail!"); 
  	dstbuf = frame->data[0];
    LOGI("ffmpeg_mjpeg2rgb 06.");
    //-- do sws scale

    return 0;
}


int ffmpeg_mjpeg2rgb2(void *srcbuf, void *dstbuf, int bufSize)
{
   //http://stackoverflow.com/questions/23443322/decoding-mjpeg-with-libavcodec
    // mJPEG to I420 conversion
    /*
    uint8_t *out_buffer = avpicture_get_size(PIX_FMT_RGB24, _transcoder->width, _transcoder->height);
    avpicture_fill((AVPicture *)pFrameYUV, out_buffer, PIX_FMT_RGB24, _transcoder->width, _transcoder->height);

    size = avpicture_get_size(_transcoder->pix_fmt, _transcoder->width, _transcoder->height);
    LOGI("size=%d bufSize=%d.",size,bufSize);
    _inputPacket.size = bufSize;
    _inputPacket.data = inputBuf;

    int got_picture;
    int decompressed_size = avcodec_decode_video2(_transcoder, _outputFrame, &got_picture, &_inputPacket);
    //outBuf = _outputFrame->data;
    LOGI(" got_picture=%d.linesize[0]=%d,[1]=%d,[2]=%d",got_picture,_outputFrame->linesize[0],_outputFrame->linesize[1],_outputFrame->linesize[2]);
    LOGI(" _outputFrame-width = %d , height = %d,format=%d,keyF=%d",_outputFrame->width,_outputFrame->height,_outputFrame->format,_outputFrame->key_frame);

    // transform
    sws_scale(mJPEGconvertCtx, _outputFrame->data, _outputFrame->linesize, 0, _transcoder->height,
            pFrameYUV->data, pFrameYUV->linesize);
outBuf = pFrameYUV->data;
    av_free(out_buffer);*/
/*
if(decFinished){
img_convert_ctx = sws_getContext(pCodecCtx->width, pCodecCtx->height,pCodecCtx->pix_fmt,
pCodecCtx->width,
pCodecCtx->height,PIX_FMT_BGR24, SWS_BICUBIC, NULL, NULL, NULL);
if (img_convert_ctx == NULL)
{
return -1;
}
pFrame->data[0] += pFrame->linesize[0] * (pCodecCtx->height - 1);
pFrame->linesize[0] *= -1;
pFrame->data[1] += pFrame->linesize[1] * (pCodecCtx->height / 2 - 1);
pFrame->linesize[1] *= -1;
pFrame->data[2] += pFrame->linesize[2] * (pCodecCtx->height / 2 - 1);
pFrame->linesize[2] *= -1;
sws_scale(img_convert_ctx, pFrame->data, pFrame->linesize,0,pCodecCtx->height, pFrameRGB->data,pFrameRGB->linesize);
//	if (decode_size)
if(decFinished)
{
}
//free(buffer);
}
*/
}

int ffmpeg_yuv2rgb(void *inputBuf, void *outBuf, int bufSize)
{
    FILE *src_file =fopen("/sdcard/DCIM/ffmpeg/yuv.jpg", "rb");
	//FILE *src_file =fopen("/sdcard/DCIM/ffmpeg/mjpeg.jpg", "rb");
    const int src_w=1280,src_h=720;
    enum AVPixelFormat src_pixfmt=AV_PIX_FMT_YUYV422;//AV_PIX_FMT_YUV420P; AV_PIX_FMT_YUVJ420P

    int src_bpp=av_get_bits_per_pixel(av_pix_fmt_desc_get(src_pixfmt));

    FILE *dst_file = fopen("/sdcard/DCIM/ffmpeg/sintel_1280x720_rgb24.mj2rgb-2", "wb");
    const int dst_w=1280,dst_h=720;
    enum AVPixelFormat dst_pixfmt=AV_PIX_FMT_RGBA;//AV_PIX_FMT_RGB24;
    int dst_bpp=av_get_bits_per_pixel(av_pix_fmt_desc_get(dst_pixfmt));

    //Structures
    uint8_t *src_data[4];
    int src_linesize[4];

    uint8_t *dst_data[4];
    int dst_linesize[4];

    int rescale_method=SWS_BICUBIC;
    struct SwsContext *img_convert_ctx;
    uint8_t *temp_buffer=(uint8_t *)malloc(src_w*src_h*src_bpp/8);

LOGE("ffmpeg_yuv2rgb 1 \n");
    int frame_idx=0;
    int ret=0;
    ret= av_image_alloc(src_data, src_linesize,src_w, src_h, src_pixfmt, 1);
    if (ret< 0) {
        LOGE("Could not allocate source image\n");
        return -1;
    }
    ret = av_image_alloc(dst_data, dst_linesize,dst_w, dst_h, dst_pixfmt, 1);
    if (ret< 0) {
        LOGE("Could not allocate destination image\n");
        return -1;
    }
    //-----------------------------
    //Init Method 1
    img_convert_ctx =sws_alloc_context();
    //Show AVOption
    //av_opt_show2(img_convert_ctx,stdout,AV_OPT_FLAG_VIDEO_PARAM,0);
    //Set Value
    av_opt_set_int(img_convert_ctx,"sws_flags",SWS_BICUBIC|SWS_PRINT_INFO,0);
    av_opt_set_int(img_convert_ctx,"srcw",src_w,0);
    av_opt_set_int(img_convert_ctx,"srch",src_h,0);
    av_opt_set_int(img_convert_ctx,"src_format",src_pixfmt,0);
    //'0' for MPEG (Y:0-235);'1' for JPEG (Y:0-255)
    av_opt_set_int(img_convert_ctx,"src_range",1,0);
    av_opt_set_int(img_convert_ctx,"dstw",dst_w,0);
    av_opt_set_int(img_convert_ctx,"dsth",dst_h,0);
    av_opt_set_int(img_convert_ctx,"dst_format",dst_pixfmt,0);
    av_opt_set_int(img_convert_ctx,"dst_range",1,0);
    sws_init_context(img_convert_ctx,NULL,NULL);

LOGE("ffmpeg_yuv2rgb 2 \n");
    //Init Method 2
    //img_convert_ctx = sws_getContext(src_w, src_h,src_pixfmt, dst_w, dst_h, dst_pixfmt,
    //  rescale_method, NULL, NULL, NULL);
    //-----------------------------
    /*
    //Colorspace
    ret=sws_setColorspaceDetails(img_convert_ctx,sws_getCoefficients(SWS_CS_ITU601),0,
        sws_getCoefficients(SWS_CS_ITU709),0,
         0, 1 << 16, 1 << 16);
    if (ret==-1) {
        printf( "Colorspace not support.\n");
        return -1;
    }
    */
    while(1)
    {
        if (fread(temp_buffer, 1, src_w*src_h*src_bpp/8, src_file) != src_w*src_h*src_bpp/8){
            break;
        }

		LOGE("while 1 \n");
        switch(src_pixfmt){
        case AV_PIX_FMT_GRAY8:{
            memcpy(src_data[0],temp_buffer,src_w*src_h);
            break;
                              }
        case AV_PIX_FMT_YUV420P:{
            memcpy(src_data[0],temp_buffer,src_w*src_h);                    //Y
            memcpy(src_data[1],temp_buffer+src_w*src_h,src_w*src_h/4);      //U
            memcpy(src_data[2],temp_buffer+src_w*src_h*5/4,src_w*src_h/4);  //V
            break;
                                }
        case AV_PIX_FMT_YUV422P:{
            memcpy(src_data[0],temp_buffer,src_w*src_h);                    //Y
            memcpy(src_data[1],temp_buffer+src_w*src_h,src_w*src_h/2);      //U
            memcpy(src_data[2],temp_buffer+src_w*src_h*3/2,src_w*src_h/2);  //V
            break;
                                }
        case AV_PIX_FMT_YUV444P:{
            memcpy(src_data[0],temp_buffer,src_w*src_h);                    //Y
            memcpy(src_data[1],temp_buffer+src_w*src_h,src_w*src_h);        //U
            memcpy(src_data[2],temp_buffer+src_w*src_h*2,src_w*src_h);      //V
            break;
                                }
        case AV_PIX_FMT_YUYV422:{
            memcpy(src_data[0],temp_buffer,src_w*src_h*2);                  //Packed
            break;
                                }
        case AV_PIX_FMT_RGB24:{
            memcpy(src_data[0],temp_buffer,src_w*src_h*3);                  //Packed
            break;
                                }
        default:{
            LOGI("Not Support Input Pixel Format.\n");
            break;
                              }
        }

        sws_scale(img_convert_ctx, src_data, src_linesize, 0, src_h, dst_data, dst_linesize);
        LOGI("Finish process frame %5d\n",frame_idx);
        frame_idx++;

        switch(dst_pixfmt){
        case AV_PIX_FMT_GRAY8:{
            fwrite(dst_data[0],1,dst_w*dst_h,dst_file);
            break;
                              }
        case AV_PIX_FMT_YUV420P:{
            fwrite(dst_data[0],1,dst_w*dst_h,dst_file);                 //Y
            fwrite(dst_data[1],1,dst_w*dst_h/4,dst_file);               //U
            fwrite(dst_data[2],1,dst_w*dst_h/4,dst_file);               //V
            break;
                                }
        case AV_PIX_FMT_YUV422P:{
            fwrite(dst_data[0],1,dst_w*dst_h,dst_file);                 //Y
            fwrite(dst_data[1],1,dst_w*dst_h/2,dst_file);               //U
            fwrite(dst_data[2],1,dst_w*dst_h/2,dst_file);               //V
            break;
                                }
        case AV_PIX_FMT_YUV444P:{
            fwrite(dst_data[0],1,dst_w*dst_h,dst_file);                 //Y
            fwrite(dst_data[1],1,dst_w*dst_h,dst_file);                 //U
            fwrite(dst_data[2],1,dst_w*dst_h,dst_file);                 //V
            break;
                                }
        case AV_PIX_FMT_YUYV422:{
            fwrite(dst_data[0],1,dst_w*dst_h*2,dst_file);               //Packed
            break;
                                }
        case AV_PIX_FMT_RGB24:{
            fwrite(dst_data[0],1,dst_w*dst_h*3,dst_file);               //Packed
            break;
                              }
		case AV_PIX_FMT_RGBA:{
			LOGI("AV_PIX_FMT_RGBA write");
			char *add1 = dst_data[0];
			//for(int i=0;i<dst_w*dst_h*4;i++)
			//	*add1[i] = *add1[i] + 1;
			outBuf = dst_data[0];
            fwrite(dst_data[0],1,dst_w*dst_h*4,dst_file);               //Packed
            break;
                              }
        default:{
            LOGE("Not Support Output Pixel Format.\n");
            break;
                            }
        }
    }

    sws_freeContext(img_convert_ctx);

    free(temp_buffer);
    fclose(dst_file);
    av_freep(&src_data[0]);
    av_freep(&dst_data[0]);

    return 0;

}