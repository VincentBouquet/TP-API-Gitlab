import com.sun.net.httpserver.HttpServer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Auth {
    private String appId = "5feaadb736a953dd847da86c0c0e484c5a907bb73a654640a5239c6283592f29";
    private String secret = "9896d775770a156ce1fdab264c4e9d6cbd34a9882020fdc4ce095d21a756b300";
    private String redirectUri = "http://localhost:8888";

    private HttpClient client;
    private HttpServer server;

    public Auth() {
        client = HttpClient.newHttpClient();
    }

    public void openAuthorizationPage() {
        //https://gitlab.com/oauth/authorize?client_id=5feaadb736a953dd847da86c0c0e484c5a907bb73a654640a5239c6283592f29&redirect_uri=http://localhost:8888&response_type=code&scope=api
        String url = "https://gitlab.com/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=api";
        String finalUrl = String.format(url, appId, redirectUri);

        try {
            Desktop.getDesktop().browse(new URI(finalUrl));
        } catch (Exception e) {
            System.out.println("Merci d'ouvrir ce lien : "+finalUrl);
        }
    }

    public void runHttpServer(){
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", 8888),0);
            server.createContext("/",new MyHandler(this));
            server.start();
        } catch (IOException e){
            System.out.println("Oops, there was an error!!");
        }
    }

    public void sendAuthCode(String code){
        server.stop(0);

        String parameters = "client_id=%s&client_secret=%s&code=%s&grant_type=authorization_code&redirect_uri=%s";
        String finalParameters = String.format(parameters, appId, secret, code, redirectUri);
        String url = "https://gitlab.com/oauth/token";
        System.out.println("finalParameters = "+finalParameters);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(finalParameters))
                    .uri(new URI(url))
                    .build();
            HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
            String content = response.body();

            System.out.println("POST Content : "+content);

            JSONObject data = (JSONObject) JSONValue.parse(content);
            String accessToken = (String) data.get("access_token");
            String refreshToken = (String) data.get("refresh_token");

            System.out.println("accessToken : "+accessToken+"\nrefreshToken : "+refreshToken);
            /*getData("https://gitlab.com/api/v4/projects?owned=true",accessToken);*/

            showUserProjects(accessToken);
            showCommitCount(accessToken);

        } catch (URISyntaxException |IOException|InterruptedException e){
            System.out.println(e);
        }
    }

    public JSONArray getData (String url, String accessToken){
        JSONArray dataArray = new JSONArray();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(new URI(url))
                    .header("Authorization", "Bearer " + accessToken)
                    .build();
            HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());
            System.out.println("response : "+response);
            dataArray = (JSONArray) (JSONValue.parse(response.body()));
        } catch (URISyntaxException|IOException|InterruptedException e) {
            e.printStackTrace();
        }
        return dataArray;
    }

    public void showUserProjects(String accessToken){

        JSONArray dataArray = getData("https://gitlab.com/api/v4/projects?owned=true",accessToken);

        String projectNameList = "Liste des projet : \n";
        for (Object project: dataArray) {
            JSONObject projectData = (JSONObject) project;
            String projectName = projectData.get("name").toString();
            projectNameList = projectNameList + projectName + "\n";
        }
        System.out.println(projectNameList);
    }

    public void showCommitCount(String accessToken){

        JSONArray dataArray = getData("https://gitlab.com/api/v4/projects?owned=true&statistics=true",accessToken);

        System.out.println("dataArraystat = "+dataArray);
        //dataArray.get("statistics")
    }

}
