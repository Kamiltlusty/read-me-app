package pl.kamil.readmeapi.reader;

import java.util.Scanner;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.Engine;
import software.amazon.awssdk.services.polly.model.LanguageCode;
import software.amazon.awssdk.services.polly.model.VoiceId;

public class Main {
    static void main() {
        Scanner s = new Scanner(System.in);
        System.out.println("Write down your sentence:");
        String text = s.nextLine();
        PollyClient polly = PollyClient.builder()
                .region(Region.EU_CENTRAL_1)
                .build();
        Reader reader = new Reader(polly);
//        reader.displayAvailableVoices("standard", LanguageCode.EN_US);
        reader.talkPolly(text, Engine.STANDARD, LanguageCode.EN_US, VoiceId.MATTHEW);
        polly.close();
    }
}
