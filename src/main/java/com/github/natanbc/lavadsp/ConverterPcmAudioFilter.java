/*
 * Copyright 2018 natanbc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.natanbc.lavadsp;

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter;

@SuppressWarnings("WeakerAccess")
public class ConverterPcmAudioFilter<T extends Converter> implements FloatPcmAudioFilter {
    private static final int DEFAULT_BUFFER_SIZE = 4096;

    protected final T converter;
    protected final FloatPcmAudioFilter downstream;
    protected final float[][] outputSegments;
    protected final int bufferSize;

    public ConverterPcmAudioFilter(T converter, FloatPcmAudioFilter downstream, int channelCount, int bufferSize) {
        this.converter = converter;
        this.downstream = downstream;
        this.bufferSize = bufferSize;
        if(bufferSize < 1) {
            this.outputSegments = null;
        } else {
            this.outputSegments = new float[channelCount][];
            for(int i = 0; i < channelCount; i++) {
                outputSegments[i] = new float[bufferSize];
            }
        }
    }

    public ConverterPcmAudioFilter(T converter, FloatPcmAudioFilter downstream, int channelCount) {
        this(converter, downstream, channelCount, DEFAULT_BUFFER_SIZE);
    }

    public T getConverter() {
        return converter;
    }

    @Override
    public void process(float[][] input, int offset, int length) throws InterruptedException {
        if(outputSegments == null) {
            for(float[] f : input) {
                converter.process(f, offset, f, 0, length);
            }
            downstream.process(input, 0, length);
        } else {
            int l = length;
            while(l > 0) {
                int size = Math.min(l, bufferSize);
                for(int i = 0; i < input.length; i++) {
                    converter.process(input[i], offset, outputSegments[i], 0, size);
                }
                downstream.process(outputSegments, 0, size);
                l -= bufferSize;
            }
        }
    }

    @Override
    public void seekPerformed(long requestedTime, long providedTime) {
        //nothing to do here
    }

    @Override
    public void flush() {
        //nothing to do here
    }

    @Override
    public void close() {
        converter.close();
    }

    @Deprecated
    @Override
    protected void finalize() throws Throwable {
        converter.close();
        super.finalize();
    }
}
