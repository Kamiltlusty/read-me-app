package pl.kamil.readmeapi.reader;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.*;

import java.io.IOException;
import java.io.InputStream;

public class Reader {
    private static final Logger logger = LoggerFactory.getLogger(Reader.class);
    private final PollyClient polly;

    public Reader(PollyClient polly) {
        this.polly = polly;
    }

    /**
     * sets api settings, calls api with given text and plays audio
     * @param text - "to be or not to be"
     * @param engine type - {standard, generative, neural}
     * @param languageCode code - PL_PL, EN_US
     * @param voiceId - voice - Ewa, Matthew
     */
    public void talkPolly(String text, Engine engine, LanguageCode languageCode, VoiceId voiceId) {
        try {
            DescribeVoicesRequest describeVoiceRequest = DescribeVoicesRequest.builder()
                    .engine(engine)
                    .languageCode(languageCode)
                    .build();

            DescribeVoicesResponse describeVoicesResult = polly.describeVoices(describeVoiceRequest);
            Voice voice = describeVoicesResult.voices().stream()
                    .filter(v -> v.id().equals(voiceId))
                    .findFirst()
                    .orElseThrow(() -> {
                        String msg = "Voice not found";
                        logger.error(msg);
                        return new RuntimeException(msg);
                    });
            InputStream stream = synthesize(polly, text, voice, OutputFormat.MP3);

            AdvancedPlayer player = new AdvancedPlayer(stream,
                    javazoom.jl.player.FactoryRegistry.systemRegistry().createAudioDevice());
            player.setPlayBackListener(new PlaybackListener() {
                public void playbackStarted(PlaybackEvent evt) {
                    logger.info("Playback started.");
                }
                public void playbackFinished(PlaybackEvent evt) {
                    logger.info("Playback finished.");
                }
            });
            player.play();
        } catch (PollyException | JavaLayerException | IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     *
     * @param voiceType String(standard, neural, long-form or generative)
     * @param languageCode LanguageCode.PL_PL
     */
    public void displayAvailableVoices(String voiceType, LanguageCode languageCode) {
        try {
            DescribeVoicesRequest describeVoiceRequest = DescribeVoicesRequest.builder()
                    .engine(voiceType)
                    .languageCode(languageCode)
                    .build();
            DescribeVoicesResponse describeVoicesResult = polly.describeVoices(describeVoiceRequest);
            describeVoicesResult.voices().stream()
                    .forEach((v) -> System.out.println(v.toString()));

        } catch (PollyException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public static InputStream synthesize(PollyClient polly, String text, Voice voice, OutputFormat format)
            throws IOException {
        SynthesizeSpeechRequest synthReq = SynthesizeSpeechRequest.builder()
                .text(text)
                .voiceId(voice.id())
                .outputFormat(format)
                .build();

        ResponseInputStream<SynthesizeSpeechResponse> synthRes = polly.synthesizeSpeech(synthReq);
        return synthRes;
    }
}
