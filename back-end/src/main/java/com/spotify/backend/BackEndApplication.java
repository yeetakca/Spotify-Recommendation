package com.spotify.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class BackEndApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackEndApplication.class, args);
    }

    @GetMapping("/")
    public String sayHello() {
        return ("Hello World!");
    }

//    @GetMapping("/token")
//    public String sayToken() {
//        SpotifyService spotifyService = new SpotifyService();
//        return (spotifyService.getAccessToken());
//    }

//    @GetMapping("/track")
//    public String giveTrack() {
//        SpotifyService spotifyService = new SpotifyService();
//        String trackId;
//        String track = null;
//        try {
//            trackId = "11dFghVXANMlKmJXsNCbNl";
//            track = spotifyService.getTrack(trackId);
//            return (track);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return (track);
//    }
//    @GetMapping("/features")
//    public String giveFeatures() {
//        SpotifyService spotifyService = new SpotifyService();
//        String trackId;
//        String features = null;
//        try {
//            trackId = "11dFghVXANMlKmJXsNCbNl";
//            features = spotifyService.getFeatures(trackId);
//            return (features);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return (features);
//    }
    @CrossOrigin
    @GetMapping("/trackrec")
    public String giveRecs(@RequestParam String id) {
        SpotifyService spotifyService = new SpotifyService();
        String trackId;
        String recs = null;
        try {
            trackId = id;
            recs = spotifyService.getRecs(trackId);
            return (recs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (recs);
    }
    @CrossOrigin
    @GetMapping("/playlistrec")
    public String giveReccs(@RequestParam String id) {

        SpotifyService spotifyService = new SpotifyService();
        String playlistId;
        String reccs = null;
        try {
            playlistId = id;
            reccs = spotifyService.getReccs(playlistId);
            return (reccs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (reccs);
    }
}
