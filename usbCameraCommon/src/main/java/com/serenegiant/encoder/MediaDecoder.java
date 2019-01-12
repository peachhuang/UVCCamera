package com.serenegiant.encoder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

public class MediaDecoder {
	private MediaCodec video_decoder;
	private MediaCodec audio_decoder;
	private Surface surface;

	private boolean isRunning = true;
	private int fps = 0;
	private int state = 0; // 0: live, 1: playback, 2: local file
	private long TIMEOUT_US = 10000;

	private ByteBuffer[] inputBuffers;
	private ByteBuffer[] outputBuffers;
	private ByteBuffer[] inputBuffersAudio;
	private ByteBuffer[] outputBuffersAudio;
	private AudioTrack audioTrack = null;
	private MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
	private MediaCodec.BufferInfo infoAudio = new MediaCodec.BufferInfo();
	private BlockingQueue<byte[]> audio_data_Queue = new ArrayBlockingQueue<byte[]>(10000);
	public static final byte[] SPS_HD = {0,0,0,1,103, 100, 64, 41, -84, 44, -88, 5, 0, 91, -112 };

	public MediaDecoder(Surface surface, int playerState) {
		this.surface = surface;
		this.state = playerState;
	}

	public boolean stopRunning() {
		try {
			isRunning = false;
			if (video_decoder != null) {
				video_decoder.flush();
				video_decoder.stop();
				video_decoder.release();
				video_decoder = null;
			}
			if (audio_data_Queue != null) {
				audio_data_Queue.clear();
			}
			return true;
		} catch (Exception e) {

		}
		return false;
	}

	public int getFPS() {
		fps=frameCount;
		frameCount=0;
		return fps;
	}

	// for FullHD
	public boolean initial() {
		MediaFormat format = null;
		video_decoder = null;
		format = MediaFormat.createVideoFormat("video/avc", 1920, 1080);
		byte[] header_sps = { 0, 0, 0, 1, 103, 100, 64, 41, -84, 44, -88, 7, -128, 34, 126, 84 };
		byte[] header_pps = { 0, 0, 0, 1, 104, -18, 56, -128 };
		format.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
		format.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
		format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1920 * 1080);
		format.setInteger("durationUs", 63446722);
		format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);

		try {
			video_decoder = MediaCodec.createDecoderByType("video/avc");
		} catch (IOException e) {
			e.printStackTrace();
			video_decoder=null;
		}

		if (video_decoder == null) {
			return false;
		}

		video_decoder.configure(format, surface, null, MediaCodec.CRYPTO_MODE_UNENCRYPTED);
		video_decoder.start();
		inputBuffers = video_decoder.getInputBuffers();
		outputBuffers = video_decoder.getOutputBuffers();
		frameCount = 0;
		deltaTime = 0;
		return true;
	}

	public void releaseDecoder() {
		video_decoder.stop();
		video_decoder.release();
		video_decoder = null;
	}

	public boolean initial(int width, int height, int frameRate, byte[] header_sps, byte[] header_pps) {
		MediaFormat format = null;
		video_decoder = null;
		
		format = MediaFormat.createVideoFormat("video/avc", width, height);
		format.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
		format.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
		format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, width * height);
		format.setInteger("durationUs", 63446722);
		format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);

		try {
			video_decoder = MediaCodec.createDecoderByType("video/avc");
		} catch (IOException e) {
			e.printStackTrace();
			video_decoder=null;
		}

		if (video_decoder == null) {
			return false;
		}

		video_decoder.configure(format, surface, null, MediaCodec.CRYPTO_MODE_UNENCRYPTED);
		video_decoder.start();
		video_decoder.flush();
		inputBuffers = video_decoder.getInputBuffers();
		outputBuffers = video_decoder.getOutputBuffers();
		frameCount = 0;
		deltaTime = 0;
		return true;
	}
	
	
	public boolean initialHD() {
		MediaFormat format = null;
		video_decoder = null;

		try {
			video_decoder = MediaCodec.createDecoderByType("video/avc");
		} catch (IOException e) {
			e.printStackTrace();
			video_decoder=null;
		}
		format = MediaFormat.createVideoFormat("video/avc", 1280, 720);
		byte[] header_sps = { 0, 0, 0, 1, 103, 100, 64, 41, -84, 44, -88, 5, 0, 91, -112 };
		byte[] header_pps = { 0, 0, 0, 1, 104, -18, 56, -128 };
		format.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
		format.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
		format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1280 * 720);
		format.setInteger("durationUs", 63446722);
		format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);

		if (video_decoder == null) {
			return false;
		}

		video_decoder.configure(format, surface, null, MediaCodec.CRYPTO_MODE_UNENCRYPTED);
		video_decoder.start();
		video_decoder.flush();
		inputBuffers = video_decoder.getInputBuffers();
		outputBuffers = video_decoder.getOutputBuffers();
		frameCount = 0;
		deltaTime = 0;
		return true;
	}

	public boolean initialVGA() {
		MediaFormat format = null;
		video_decoder = null;
		
		format = MediaFormat.createVideoFormat("video/avc", 1920, 1080);
		byte[] header_sps = { 0, 0, 0, 1, 103, 100, 64, 41, -84, 44, -88, 10, 2, -1, -107 };
		byte[] header_pps = { 0, 0, 0, 1, 104, -18, 56, -128 };
		format.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
		format.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
		format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 640 * 360);
		format.setInteger("durationUs", 63446722);
		format.setInteger(MediaFormat.KEY_FRAME_RATE, 15);

		try {
			video_decoder = MediaCodec.createDecoderByType("video/avc");
		} catch (IOException e) {
			e.printStackTrace();
			video_decoder=null;
		}

		if (video_decoder == null) {
			return false;
		}

		video_decoder.configure(format, surface, null, MediaCodec.CRYPTO_MODE_UNENCRYPTED);
		video_decoder.start();
		inputBuffers = video_decoder.getInputBuffers();
		outputBuffers = video_decoder.getOutputBuffers();
		frameCount = 0;
		deltaTime = 0;
		return true;
	}

	int frameCount = 0;
	long deltaTime = 0;
	@SuppressLint("NewApi")
	public void decode(byte[] data) {
		int inIndex = -1;
		try {
			inIndex = video_decoder.dequeueInputBuffer(TIMEOUT_US);
		} catch (Exception e) {
			return;
		}

		try {
			long counterTime = System.currentTimeMillis();
			if (inIndex >= 0) {
				ByteBuffer buffer = inputBuffers[inIndex];
				buffer.clear();
				if (data != null) {
					buffer.put(data);
					if (state == 0) {
						video_decoder.queueInputBuffer(inIndex, 0, data.length, 66, 0);
					} else {
						video_decoder.queueInputBuffer(inIndex, 0, data.length, 33, 0);
					}
				} else {
					if (state == 0) {
						video_decoder.queueInputBuffer(inIndex, 0, 0, 66, 0);
					} else {
						video_decoder.queueInputBuffer(inIndex, 0, 0, 33, 0);
					}
				}
			}

			int outIndex = video_decoder.dequeueOutputBuffer(info, 0);
			switch (outIndex) {
			case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
				outputBuffers = video_decoder.getOutputBuffers();
				break;
			case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
				break;
			case MediaCodec.INFO_TRY_AGAIN_LATER:
				break;
			default:
				ByteBuffer buffer = outputBuffers[outIndex];
				video_decoder.releaseOutputBuffer(outIndex, true);
				frameCount++;
				deltaTime = System.currentTimeMillis() - counterTime;
				break;
			}

			// All decoded frames have been rendered, we can stop playing now
			if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
				Log.d("Decoder", "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
			}
		} catch (Exception e) {

		}
	}

	public boolean initialAudio() {// AACObjectLC
		int sampleRate = 11025;
		MediaFormat format = makeAACCodecSpecificData(MediaCodecInfo.CodecProfileLevel.AACObjectLC, sampleRate, 1);
		if (format == null)
			return false;

		try {
			audio_decoder = MediaCodec.createDecoderByType("audio/mp4a-latm");
		} catch (Exception e) {
			Log.e("Decoder", "AAC create decoder exception: " + e.toString());
			return false;
		}

		audio_decoder.configure(format, null, null, 0);
		if (audio_decoder == null) {
			Log.e("Decoder", "Can't find audio info!");
			return false;
		}
		audio_decoder.start();

		inputBuffersAudio = audio_decoder.getInputBuffers();
		outputBuffersAudio = audio_decoder.getOutputBuffers();

		infoAudio = new MediaCodec.BufferInfo();

		int buffsize = 0;
		audioTrack = null;
		buffsize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, buffsize, AudioTrack.MODE_STREAM);
		audioTrack.play();
		return true;
	}

	public void setAudioData(byte[] data) {
		try {
			audio_data_Queue.put(data);
		} catch (InterruptedException e) {

		}
	}

	public void decodeAudio(byte[] data) {
		int inIndex = audio_decoder.dequeueInputBuffer(-1);
		if (inIndex >= 0) {
			ByteBuffer buffer = inputBuffersAudio[inIndex];
			buffer.clear();

			if (data != null) {
				buffer.put(data);
				audio_decoder.queueInputBuffer(inIndex, 0, data.length, 20, 0);
			} else {
				audio_decoder.queueInputBuffer(inIndex, 0, 0, 20, 0);
			}
		} else {
			audio_decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
		}

		int outIndex = audio_decoder.dequeueOutputBuffer(infoAudio, 0);
		switch (outIndex) {
		case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
			outputBuffersAudio = audio_decoder.getOutputBuffers();
			break;
		case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
			MediaFormat new_format = audio_decoder.getOutputFormat();
			audioTrack.setPlaybackRate(new_format.getInteger(MediaFormat.KEY_SAMPLE_RATE));
			break;
		case MediaCodec.INFO_TRY_AGAIN_LATER:
			break;
		default:
			ByteBuffer outBuffer = outputBuffersAudio[outIndex];
			final byte[] chunk = new byte[infoAudio.size];
			outBuffer.get(chunk);
			outBuffer.clear();
			audioTrack.write(chunk, infoAudio.offset, infoAudio.offset + infoAudio.size);
			audio_decoder.releaseOutputBuffer(outIndex, false);
			break;
		}

		// All decoded frames have been rendered, we can stop playing now
		if ((infoAudio.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
			Log.d("Decoder", "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
		}
	}

	public void runAudioThread(int i) {
		Thread t = new Thread() {
			public void run() {
				int sampleRate = 11025;
				MediaFormat format = makeAACCodecSpecificData(MediaCodecInfo.CodecProfileLevel.AACObjectLC, sampleRate, 1);
				if (format == null)
					return;

				try {
					audio_decoder = MediaCodec.createDecoderByType("audio/mp4a-latm");
				} catch (Exception e) {

				}

				audio_decoder.configure(format, null, null, 0);
				if (audio_decoder == null) {
					Log.e("Decoder", "Can't find audio info!");
					return;
				}
				audio_decoder.start();

				ByteBuffer[] inputBuffersAudio = audio_decoder.getInputBuffers();
				ByteBuffer[] outputBuffersAudio = audio_decoder.getOutputBuffers();

				MediaCodec.BufferInfo infoAudio = new MediaCodec.BufferInfo();

				int buffsize = 0;
				AudioTrack audioTrack = null;
				buffsize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
				audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, buffsize, AudioTrack.MODE_STREAM);
				audioTrack.play();

				while (isRunning) {
					try {
						int inIndex = audio_decoder.dequeueInputBuffer(1000);
						if (inIndex >= 0) {
							ByteBuffer buffer = inputBuffersAudio[inIndex];
							buffer.clear();

							if (!audio_data_Queue.isEmpty()) {
								byte[] data;
								try {
									data = audio_data_Queue.take();
									buffer.put(data);
									audio_decoder.queueInputBuffer(inIndex, 0, data.length, 20, 0);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							} else {
								audio_decoder.queueInputBuffer(inIndex, 0, 0, 20, 0);
							}
						} else {
							audio_decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
						}

						int outIndex = audio_decoder.dequeueOutputBuffer(infoAudio, 0);
						switch (outIndex) {
						case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
							outputBuffersAudio = audio_decoder.getOutputBuffers();
							break;
						case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
							MediaFormat new_format = audio_decoder.getOutputFormat();
							audioTrack.setPlaybackRate(new_format.getInteger(MediaFormat.KEY_SAMPLE_RATE));
							break;
						case MediaCodec.INFO_TRY_AGAIN_LATER:
							break;
						default:
							ByteBuffer outBuffer = outputBuffersAudio[outIndex];
							final byte[] chunk = new byte[infoAudio.size];
							outBuffer.get(chunk);
							outBuffer.clear();
							audioTrack.write(chunk, infoAudio.offset, infoAudio.offset + infoAudio.size);
							audio_decoder.releaseOutputBuffer(outIndex, false);
							break;
						}

						// All decoded frames have been rendered, we can stop playing now
						if ((infoAudio.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
							Log.d("Decoder", "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
							break;
						}
					} catch (Exception e) {

					}
				}

				audio_decoder.stop();
				audio_decoder.release();
				audio_decoder = null;

				audioTrack.stop();
				audioTrack.release();
				audioTrack = null;
			}
		};
		t.start();
	}

	private MediaFormat makeAACCodecSpecificData(int audioProfile, int sampleRate, int channelConfig) {
		MediaFormat format = new MediaFormat();
		format.setString(MediaFormat.KEY_MIME, "audio/mp4a-latm");
		format.setInteger(MediaFormat.KEY_SAMPLE_RATE, sampleRate);
		format.setInteger(MediaFormat.KEY_BIT_RATE, 11025);
		format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
		format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);

		// Search the Sampling Frequencies
		int samplingFreq[] = { 96000, 88200, 64000, 48000, 44100, 32000, 24000, 22050, 16000, 12000, 11025, 8000 };
		int sampleIndex = -1;
		for (int i = 0; i < samplingFreq.length; ++i) {
			if (samplingFreq[i] == sampleRate) {
				sampleIndex = i;
			}
		}

		if (sampleIndex == -1) {
			return null;
		}

		ByteBuffer csd = ByteBuffer.allocate(2);
		csd.put((byte) ((audioProfile << 3) | (sampleIndex >> 1)));
		csd.position(1);
		csd.put((byte) ((byte) ((sampleIndex << 7) & 0x80) | (channelConfig << 3)));
		csd.flip();
		format.setByteBuffer("csd-0", csd);
		return format;
	}
}