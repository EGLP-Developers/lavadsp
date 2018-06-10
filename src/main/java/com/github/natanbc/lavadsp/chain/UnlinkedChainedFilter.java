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

package com.github.natanbc.lavadsp.chain;

import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory;
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * Chained filter whose output can be dynamically changed.
 */
@SuppressWarnings("WeakerAccess")
public class UnlinkedChainedFilter extends ChainedFilter implements PcmFilterFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnlinkedChainedFilter.class);

    /**
     * Format used when building the chain, used to verify compatibility when provided as a PcmFilterFactory
     * to an AudioPlayer.
     */
    protected final AudioDataFormat format;

    /**
     * Delegated filter, used to transparently change the actual output.
     */
    protected final DelegatedUniversalPcmAudioFilter output;

    protected UnlinkedChainedFilter(List<AudioFilter> filters, FloatPcmAudioFilter last, AudioDataFormat format, DelegatedUniversalPcmAudioFilter output) {
        super(filters, last);
        this.format = format;
        this.output = output;
    }

    /**
     * Links this filter to a new output, discarding the previous one.
     *
     * @param filter New filter to set as output.
     *
     * @return {@code this}, for chaining calls.
     */
    public UnlinkedChainedFilter link(UniversalPcmAudioFilter filter) {
        output.setDelegate(filter);
        return this;
    }

    @Override
    public List<AudioFilter> buildChain(AudioTrack track, AudioDataFormat format, UniversalPcmAudioFilter output) {
        if(!format.equals(this.format)) {
            LOGGER.error("Format mismatch: {} expected, got {}", prettyPrint(this.format), prettyPrint(format));
            throw new IllegalArgumentException("Mismatched format");
        }
        link(output);
        return Collections.singletonList(this);
    }

    private static String prettyPrint(AudioDataFormat format) {
        return "(" + format.codecName() + ", " + format.channelCount + ", " +
                format.sampleRate + ", " + format.chunkSampleCount + ")";
    }
}