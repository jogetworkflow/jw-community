package org.joget.ai.audio;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import org.joget.commons.util.LogUtil;
import org.jtransforms.fft.FloatFFT_1D;

public class Audio {
    /**
     * C-1 = 16.35 / 2 Hz.
     */
    private static final double REF_FREQ = 8.17579892;
    /**
     * Cache LOG 2 calculation.
     */
    private static final double LOG_TWO = Math.log(2.0);
        
    protected AudioInputStream ais;
    protected long framesCount;
    protected int frameSize;
    protected int sampleRate;
    protected int sampleSize;
    protected int channel;
    protected long dataLength;
    protected boolean bigEndian;
    protected String encoding;
    protected int minFrequency = 50; // Hz 
    protected int maxFrequency = 11000; // Hz
    
    protected BufferedImage bufferedImage = null;
    int position = 0;
    
    public Audio(AudioInputStream ais) {
        this.ais = ais;
        
        AudioFormat af = ais.getFormat();
        framesCount = ais.getFrameLength();
        frameSize = af.getFrameSize();
        sampleRate = (int) af.getSampleRate();
        sampleSize = af.getSampleSizeInBits();
        channel = af.getChannels();
        encoding = af.getEncoding().toString();
        bigEndian = af.isBigEndian();
    }
    
    protected int frequencyToBin(double frequency, int height) {
        int bin = 0;
        final boolean logaritmic = true;
        if (frequency != 0 && frequency > minFrequency && frequency < maxFrequency) {
            double binEstimate = 0;
            if (logaritmic) {
                final double minCent = hertzToAbsoluteCent(minFrequency);
                final double maxCent = hertzToAbsoluteCent(maxFrequency);
                final double absCent = hertzToAbsoluteCent(frequency * 2);
                binEstimate = (absCent - minCent) / maxCent * height;
            } else {
                binEstimate = (frequency - minFrequency) / maxFrequency * height;
            }
            bin = height - 1 - (int) binEstimate;
        }
        return bin;
    }
    
    protected double hertzToAbsoluteCent(double hertzValue) {
        double pitchInAbsCent = 0.0;
        if (hertzValue > 0) {
                pitchInAbsCent = 1200 * Math.log(hertzValue / REF_FREQ) / LOG_TWO;
        } else {
                throw new IllegalArgumentException("Pitch in Hz schould be greater than zero, is " + hertzValue);
        }
	return pitchInAbsCent;
    }
    
    protected void draw(float[] amplitudes, int width, int height) {
        if(position >= width){
            return;
        }
        Graphics2D bufferedGraphics = bufferedImage.createGraphics();

        double maxAmplitude=0;
        
        //for every pixel calculate an amplitude
        float[] pixelAmplitudes = new float[height];
        //iterate the large array and map to pixels
        for (int i = 0; i < amplitudes.length; i++) {
            int pixelY = frequencyToBin(i * (sampleRate / amplitudes.length / 2) , height);
            pixelAmplitudes[pixelY] += amplitudes[i];
            maxAmplitude = Math.max(pixelAmplitudes[pixelY], maxAmplitude);
        }

        //draw the pixels
        for (int i = 0; i < pixelAmplitudes.length; i++) {
            Color color = Color.black;
            if (maxAmplitude != 0) {

                final int greyValue = (int) (Math.log1p(pixelAmplitudes[i] / maxAmplitude) / Math.log1p(1.0000001) * 255);
                color = new Color(greyValue, greyValue, greyValue);
            }
            bufferedGraphics.setColor(color);
            bufferedGraphics.fillRect(position, i, 3, 1);
        }

        position+=3;
        position = position % width;
    }
    
    protected void covertToFloat(byte[] audioByteBuffer, int offsetInBytes, float[] audioFloatBuffer, int offsetInSamples, int floatStepSize) {
        int ix = offsetInBytes;
        int ox = offsetInSamples;
        int x = 0;
        if ("PCM_SIGNED".equalsIgnoreCase(encoding)) {
            for (int i = 0; i < floatStepSize; i++) {
                x = 0;
                if (sampleSize <= 8) {
                    audioFloatBuffer[ox++] = audioByteBuffer[ix++] * (1.0f / 127.0f);
                } else if (sampleSize <= 16) {
                    if (bigEndian) {
                        audioFloatBuffer[ox++] = ((short) ((audioByteBuffer[ix++] << 8) | (audioByteBuffer[ix++] & 0xFF))) * (1.0f / 32767.0f);
                    } else {
                        x = (audioByteBuffer[ix++] & 0xFF) | ((audioByteBuffer[ix++] & 0xFF) << 8);
                        audioFloatBuffer[ox++] = (x - 32767) * (1.0f / 32767.0f);
                    }
                } else if (sampleSize <= 24) {
                    if (bigEndian) {
                        x = ((audioByteBuffer[ix++] & 0xFF) << 16) | ((audioByteBuffer[ix++] & 0xFF) << 8) | (audioByteBuffer[ix++] & 0xFF);
                        if (x > 0x7FFFFF)
                            x -= 0x1000000;
                    } else {
                        x = (audioByteBuffer[ix++] & 0xFF) | ((audioByteBuffer[ix++] & 0xFF) << 8) | ((audioByteBuffer[ix++] & 0xFF) << 16);
                        x -= 0x7FFFFF;
                    }
                    audioFloatBuffer[ox++] = x * (1.0f / (float)0x7FFFFF);
                } else if (sampleSize <= 32) {
                    if (bigEndian) {
                        x = ((audioByteBuffer[ix++] & 0xFF) << 24) | ((audioByteBuffer[ix++] & 0xFF) << 16) | ((audioByteBuffer[ix++] & 0xFF) << 8) | (audioByteBuffer[ix++] & 0xFF);
                    } else {
                        x = (audioByteBuffer[ix++] & 0xFF) | ((audioByteBuffer[ix++] & 0xFF) << 8) | ((audioByteBuffer[ix++] & 0xFF) << 16) | ((audioByteBuffer[ix++] & 0xFF) << 24);
                    }
                    audioFloatBuffer[ox++] = x * (1.0f / (float)0x7FFFFFFF);
                }
            }
        } else if ("PCM_UNSIGNED".equalsIgnoreCase(encoding)) {
            for (int i = 0; i < floatStepSize; i++) {
                x = 0;
                if (sampleSize <= 8) {
                    audioFloatBuffer[ox++] = ((audioByteBuffer[ix++] & 0xFF) - 127) * (1.0f / 127.0f);
                } else if (sampleSize <= 16) {
                    if (bigEndian) {
                        x = ((audioByteBuffer[ix++] & 0xFF) << 8) | (audioByteBuffer[ix++] & 0xFF);
                    } else {
                        x = (audioByteBuffer[ix++] & 0xFF) | ((audioByteBuffer[ix++] & 0xFF) << 8);
                    }
                    audioFloatBuffer[ox++] = (x - 32767) * (1.0f / 32767.0f);
                } else if (sampleSize <= 24) {
                    if (bigEndian) {
                        x = ((audioByteBuffer[ix++] & 0xFF) << 16) | ((audioByteBuffer[ix++] & 0xFF) << 8) | (audioByteBuffer[ix++] & 0xFF);
                    } else {
                        x = (audioByteBuffer[ix++] & 0xFF) | ((audioByteBuffer[ix++] & 0xFF) << 8) | ((audioByteBuffer[ix++] & 0xFF) << 16);
                    }
                    x -= 0x7FFFFF;
                    audioFloatBuffer[ox++] = x * (1.0f / (float)0x7FFFFF);
                } else if (sampleSize <= 32) {
                    if (bigEndian) {
                        x = ((audioByteBuffer[ix++] & 0xFF) << 24) | ((audioByteBuffer[ix++] & 0xFF) << 16) | ((audioByteBuffer[ix++] & 0xFF) << 8) | (audioByteBuffer[ix++] & 0xFF);
                    } else {
                        x = (audioByteBuffer[ix++] & 0xFF) | ((audioByteBuffer[ix++] & 0xFF) << 8) | ((audioByteBuffer[ix++] & 0xFF) << 16) | ((audioByteBuffer[ix++] & 0xFF) << 24);
                    }
                    x -= 0x7FFFFFFF;
                    audioFloatBuffer[ox++] = x * (1.0f / (float)0x7FFFFFFF);
                }
            }
        }
    }
    
    protected float modulus(float[] data, int index) {
        int realIndex = 2 * index;
	int imgIndex =  2 * index + 1;
	float modulus = data[realIndex] * data[realIndex] + data[imgIndex] * data[imgIndex];
	return (float) Math.sqrt(modulus);
    }
    
    protected void process(float[] audioFloatBuffer, int width, int height, int fftBufferSize, int overlapSize) {
        float[] transformBuffer = new float[fftBufferSize * 2];
        System.arraycopy(audioFloatBuffer, 0, transformBuffer, 0, audioFloatBuffer.length);
        FloatFFT_1D fft = new FloatFFT_1D(fftBufferSize);
        fft.realForward(transformBuffer);

        float[] amplitudes = new float[fftBufferSize];
        for (int i = 0; i < fftBufferSize; i++) {
            amplitudes[i] = modulus(transformBuffer, i);
	}
        
        draw(amplitudes, width, height);
    }
    
    public BufferedImage getSpectrogramImage(int width, int height, int fftBufferSize, int overlapSize, int min, int max) {
        if (bufferedImage == null) {
            minFrequency = min;
            maxFrequency = max;
            
            bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            
            try {
                float[] audioFloatBuffer = new float[fftBufferSize];
		int floatOverlap = overlapSize;
		int floatStepSize = audioFloatBuffer.length - floatOverlap;

		byte[] audioByteBuffer = new byte[audioFloatBuffer.length * frameSize];
		int byteOverlap = floatOverlap * frameSize;
		int byteStepSize = floatStepSize * frameSize;
                
                int bytesRead = 0;
                int bytesToRead = audioByteBuffer.length;
                int floatToRead = audioFloatBuffer.length;
                int offsetInBytes = 0;
                int offsetInSamples = 0;
                
                while((bytesRead = ais.read(audioByteBuffer, offsetInBytes , bytesToRead)) != -1) {
                    
                    covertToFloat(audioByteBuffer, offsetInBytes, audioFloatBuffer, offsetInSamples, floatToRead);
                    
                    process(audioFloatBuffer, width, height, fftBufferSize, overlapSize);
                    
                    if(audioFloatBuffer.length == floatOverlap + floatStepSize ){
                        System.arraycopy(audioFloatBuffer,floatStepSize, audioFloatBuffer,0 ,floatOverlap);
                    }
                    
                    bytesToRead = byteStepSize;
                    floatToRead = floatStepSize;
                    offsetInBytes = byteOverlap;
                    offsetInSamples = floatOverlap;
                }
            } catch (Exception e) {
                LogUtil.error(Audio.class.getName(), e, "");
            } finally {
                try {
                    ais.close();
                } catch (Exception e){}
            }
        }
        return bufferedImage;
    }
}
