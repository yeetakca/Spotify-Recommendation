package com.spotify.backend;
//http://10.10.6.30:4200
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class BackEndApplication {
    private final SpotifyService spotifyService;

    public BackEndApplication(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    public static void main(String[] args) {
        SpringApplication.run(BackEndApplication.class, args);
    }

    @GetMapping("/")
    public String sayHello() {
        return "Hello World!";
    }

    // Uncomment the code below if needed
    /*
    @GetMapping("/token")
    public String sayToken() {
        return spotifyService.getAccessToken();
    }

    @GetMapping("/track")
    public String giveTrack() {
        String trackId = "11dFghVXANMlKmJXsNCbNl";
        try {
            return spotifyService.getTrack(trackId);
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to get track information.";
        }
    }

    @GetMapping("/features")
    public String giveFeatures() {
        String trackId = "11dFghVXANMlKmJXsNCbNl";
        try {
            return spotifyService.getFeatures(trackId);
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to get track features.";
        }
    }
    */

    @CrossOrigin
    @GetMapping("/trackrec")
    public String giveTrackRecs(@RequestParam String id) {
        try {
            return spotifyService.getTrackRecs(id);
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to get recommendations.";
        }
    }

    @CrossOrigin
    @GetMapping("/playlistrec")
    public String givePlaylistRecs(@RequestParam String id) {
        try {
            return spotifyService.getPlaylistRec(id);
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to get playlist recommendations.";
        }
    }
}
