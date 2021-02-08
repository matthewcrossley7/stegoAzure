package com.example.demo;



import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;


import java.awt.*;
import java.io.*;
import java.util.Base64;
import java.util.Map;

//was just @Controller before
//@RestController
@Controller
public class GETController {
    String msg;
    String filePath;
    Map userAttributes;
    imageMsg[]  imageArray;
    ChatClient serverAccess;
    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="THERE FELLOW PERSON") String name, Model model) {

        model.addAttribute("name", name);

        return "greeting - Copy";
    }
    @GetMapping("/profile")
    public String displayProfile(Model model) {
        model.addAttribute("name", userAttributes.get("name"));
        model.addAttribute("email", userAttributes.get("email"));
        model.addAttribute("filePath", filePath);

        return "profile";
    }
    @PostMapping("/profile")
    public String updateProfile(Model model,@RequestParam  String newFilePath) throws JSONException, IOException {
        System.out.println("UPDATING PROFILE");
        model.addAttribute("name", userAttributes.get("name"));
        model.addAttribute("email", userAttributes.get("email"));
        model.addAttribute("filePath", newFilePath);
        filePath=newFilePath;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "updatePath");
        jsonObject.put("email", userAttributes.get("email"));
        jsonObject.put("path", newFilePath);
        serverAccess.sendJSON(jsonObject);
        JSONObject recieveJSON = serverAccess.recieveMsg();
        return "profile";
    }
    @GetMapping("/addFriend")
    public String addFriend(Model model) {
        model.addAttribute("email", userAttributes.get("email"));
        model.addAttribute("name", userAttributes.get("name"));
        return "addFriend";
    }
    @PostMapping("/addFriend")
    public String addFriendPost(Model model,@RequestParam  String friendEmail) throws JSONException, IOException {
        System.out.println(userAttributes.get("email")+" wants to add "+friendEmail);
        if(!friendEmail.equals(userAttributes.get("email"))){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "addFriend");
            jsonObject.put("userEmail", userAttributes.get("email"));
            System.out.println("AYAYY " + userAttributes.get("email"));
            jsonObject.put("friendEmail", friendEmail);
            serverAccess.sendJSON(jsonObject);
            JSONObject recieveJSON = serverAccess.recieveMsg();
        }else{
            System.out.println("YOU CANT ADD URSELF");
            model.addAttribute("name", "YO CANT ADDURSELF");
        }
        return "addFriend";
    }
    public static String encodeImage(byte[] imageByteArray) {
        return Base64.getEncoder().encodeToString(imageByteArray);
    }
    @GetMapping("/send")
    public String send(Model model,@RequestParam(name="email", required=false, defaultValue="null") String email,@RequestParam(defaultValue = "")  String status,@RequestParam(defaultValue = "")  String fileSent) throws JSONException, IOException {
        model.addAttribute("toEmail", email);
        model.addAttribute("image", "C:\\Users\\mcrossley\\IdeaProjects\\Stego Project Oauth\\demo\\src\\main\\java\\com\\example\\demo\\download.png");
        model.addAttribute("email", userAttributes.get("email"));
        model.addAttribute("name", userAttributes.get("name"));
        if(status.equals("success")){
            model.addAttribute("status","success");
            model.addAttribute("sentFile",fileSent);
        }else if(status.equals("neither")){
            model.addAttribute("status","neither");
        }else if(status.equals("noImage")){
            model.addAttribute("status","noImage");
        }else if(status.equals("noEmail")){
            model.addAttribute("status","noEmail");
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","viewFriends");
        jsonObject.put("email",userAttributes.get("email"));
        serverAccess.sendJSON(jsonObject);
        JSONObject friendList = serverAccess.recieveMsg();
        System.out.println("YOU HAVE " +friendList.length() + "FRIENDS");
        String[] friendArray = new String[friendList.length()];
        for(int x=0;x<friendList.length();x++){
            friendArray[x]= friendList.getString("email"+String.valueOf(x+1));
        }
        model.addAttribute("friendArray", friendArray);


        return "send";
    }

    @PostMapping("/send")
    public RedirectView sendPost(@RequestParam(defaultValue = "null")  String fileName,@RequestParam String email,@RequestParam String friend, Model model) throws IOException, JSONException {
        System.out.println("---"+friend);
        if(friend.equals("unselected") && fileName.equals("null")){
            return new RedirectView("http://localhost:8080/send?status=neither");
        }
        if(fileName.equals("null")){
            model.addAttribute("email", email);
            System.out.println("---"+email);
            return new RedirectView("http://localhost:8080/send?status=noImage");
        }
        if(friend.equals("unselected")){
            model.addAttribute("email", email);
            System.out.println("YOU DIDNT CHOOSE AN EMAIL");
            return new RedirectView("http://localhost:8080/send?status=noEmail");
        }

        System.out.println("I GOT "+fileName+"sending to ");
        System.out.println(filePath);

        String completePath = filePath+ "\\"+ fileName;
        System.out.println(completePath);
        //image theImage = new image(completePath);
        //theImage.readImage();
        File file = new File(completePath);
        //Image conversion to byte array
        FileInputStream imageInFile = new FileInputStream(file);
        byte imageData[] = new byte[(int) file.length()];
        imageInFile.read(imageData);

        //Image conversion byte array in Base64 String
        String imageDataString = encodeImage(imageData);
        imageInFile.close();
        System.out.println("Image Successfully Manipulated!");
        System.out.println("SENDING TO EMAIL "+email);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","sendImage");
        jsonObject.put("fromEmail",userAttributes.get("email"));
        jsonObject.put("toEmail",friend);
        jsonObject.put("image",imageDataString);
       // System.out.println(imageDataString);
        serverAccess.sendJSON(jsonObject);
        JSONObject recieveJSON = serverAccess.recieveMsg();
       // model.addAttribute("greeting", greeting);
        return new RedirectView("/send?status=success&fileSent="+fileName);
    }
    @GetMapping("/viewMsgs")
    public String viewMsgs(@RequestParam(defaultValue = "")  String email,Model model,@RequestParam(defaultValue = "")  String downloadStatus,@RequestParam(defaultValue = "")  String deleteStatus,@RequestParam(defaultValue = "")  String msg) throws JSONException, IOException {
        if(email==""){
            System.out.println("FROM ANYONE");
        }else{
            System.out.println("FROM "+email);
        }
        System.out.println(downloadStatus);
        if(downloadStatus.equals("success")){
            model.addAttribute("status", "downloaded");
            model.addAttribute("newFileName", "NEW DOWNLOAD");
            System.out.println("SUCCESS DOWNLOADED"+downloadStatus);
        }else{
            System.out.println("JUST A NORMAL"+downloadStatus);
        }
        if(deleteStatus.equals("success")){
            model.addAttribute("status", "deleted");
            System.out.println("SUCCESS DOWNLOADED"+downloadStatus);
        }else{
            System.out.println("JUST A NORMAL"+downloadStatus);
        }
        System.out.println("ABOUT TO MAYBE ADD A MSG"+msg);
        if(!msg.equals("")){
            model.addAttribute("msg", msg);
            System.out.println("ADDING MSG" + msg);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","viewMsgs");
        jsonObject.put("email",userAttributes.get("email"));
        jsonObject.put("fromEmail",email);
        System.out.println("MY REQUEST "+jsonObject.toString());
        serverAccess.sendJSON(jsonObject);
        JSONObject recieveJSON = serverAccess.recieveMsg();

        String[] imageFrom = new String[recieveJSON.length()];
        String[] imageTo = new String[recieveJSON.length()];
        String[] imageTime = new String[recieveJSON.length()];
        String[] imageString = new String[recieveJSON.length()];
        imageArray = new imageMsg[recieveJSON.length()];
        for(int x=0;x<recieveJSON.length();x++){
            //imageArray[x]= recieveJSON.getString("image"+String.valueOf(x+1));
            //System.out.println(imageArray[x]);
            JSONObject imageJson = new JSONObject(recieveJSON.getString("image"+String.valueOf(x+1)));
            //System.out.println(imageJson.getString("image"));
            //System.out.println(imageJson.getString("time"));
            //System.out.println(imageJson.getString("fromEmail"));
            if(imageJson.getString("fromEmail")==userAttributes.get("email")){
                imageFrom[x]=imageJson.getString("fromEmail");
                imageTo[x]= (String) userAttributes.get("email");
            }else{
                imageTo[x]=imageJson.getString("fromEmail");
                imageFrom[x]= (String) userAttributes.get("email");
            }
              imageTime[x]=imageJson.getString("time");
            //imageString[x]=imageJson.getString("image");
            imageArray[x] = new imageMsg(imageJson.getString("fromEmail"),imageJson.getString("time"),imageJson.getString("image"),imageJson.getString("toEmail"),imageJson.getInt("imageID"));
            imageString[x]= imageJson.getString("fromEmail")+"  at time " + imageJson.getString("time");
        }
       // model.addAttribute("imageArray", imageString);
        model.addAttribute("imageTo", imageTo);
        model.addAttribute("imageFrom", imageFrom);
        model.addAttribute("imageTime", imageTime);
        model.addAttribute("imageArray", imageArray);
        model.addAttribute("fromEmail", email   );
        model.addAttribute("name", userAttributes.get("name"));
        model.addAttribute("email", userAttributes.get("email"));
        jsonObject = new JSONObject();
        jsonObject.put("type","viewFriends");
        jsonObject.put("email",userAttributes.get("email"));
        serverAccess.sendJSON(jsonObject);
        JSONObject friendList = serverAccess.recieveMsg();

        String[] friendArray = new String[friendList.length()];
        model.addAttribute("friendArray", friendArray);
        for(int x=0;x<friendList.length();x++){
            friendArray[x]= friendList.getString("email"+String.valueOf(x+1));
        }
       // model.addAttribute("imageArray", imageArray);
        return "viewMsgs";
    }
    @RequestMapping(value="/viewMsgs",params="imageString",method=RequestMethod.POST)
    public RedirectView image1(@RequestParam  String imageString,@RequestParam String fileName,@RequestParam(defaultValue = "") String email) throws IOException {
        System.out.println("IAMGEI  IS "+imageString+"   "+email);
        byte[] imageByteArray = decodeImage(imageArray[Integer.parseInt(imageString)].image);
        FileOutputStream imageOutFile = new FileOutputStream(filePath+"\\"+fileName+".png");
        imageOutFile.write(imageByteArray);
        imageOutFile.close();
        System.out.println("SUCCESSFULLY SAVED IMAGE");
        System.out.println("Image block called"+imageString);
        if(email.equals("")){
            return new RedirectView("/viewMsgs?downloadStatus=success");
        }else{
            return new RedirectView("/viewMsgs?email="+email+"&downloadStatus=success");
        }

    }
    @RequestMapping(value="/viewMsgs",params="delete",method=RequestMethod.POST)
    public RedirectView delete(@RequestParam  String delete,@RequestParam(defaultValue = "") String email) throws JSONException, IOException {
        System.out.println("delete block called"+delete);
        System.out.println("DELETE IMAGE ID " + imageArray[Integer.parseInt(delete)].imageID);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","deleteMsg");
        jsonObject.put("imageID",imageArray[Integer.parseInt(delete)].imageID);
        serverAccess.sendJSON(jsonObject);
        JSONObject result = serverAccess.recieveMsg();
        if(email.equals("")){
            return new RedirectView("/viewMsgs?deleteStatus=success");
        }else{
            return new RedirectView("/viewMsgs?email="+email+"&deleteStatus=success");
        }
    }
    @RequestMapping(value="/viewMsgs",params="decode",method=RequestMethod.POST)
    public RedirectView decode(@RequestParam  String decode,@RequestParam(defaultValue = "") String email) throws IOException {
        System.out.println("decode block called"+decode);
        image theImage = new image();
        theImage.setString(imageArray[Integer.parseInt(decode)].image);

        imageCentre center = theImage.isCentre();
        if(center==null){
            //model.addAttribute("msg","error couldnt find any message");
            return new RedirectView("/viewMsgs?email="+email+"&msg=No message found");

        }else{
            int length = theImage.getMsgLength(center);
            System.out.println("IMAGE LENGTH IS  " + length);
            System.out.println("IMAGE MESSAGE IS  " + theImage.extractData(center));
            String msg = theImage.extractData(center);
            System.out.println("IMAGE LENGTH IS  " + length);
            System.out.println("IMAGE MESSAGE IS  " + msg);

            return new RedirectView("/viewMsgs?email="+email+"&msg="+msg);
        }






    }
   /* @PostMapping("/viewMsgs")
    public String downloadMsg(Model model,@RequestParam(defaultValue = "") String decode,@RequestParam(defaultValue = "") String delete,@RequestParam(defaultValue = "") String image,@RequestParam String fileName) throws JSONException, IOException {
        System.out.println("pressed");
        System.out.println(decode + "  " + image+ "   "+delete);
        /*System.out.println("DSABUDIBUHSAHBDSABHDSAHBDSABHDSADSA " +delete);
         //   System.out.println("DOWNLLOAD  AMSG" + image);
        byte[] imageByteArray = decodeImage(image);
        FileOutputStream imageOutFile = new FileOutputStream(filePath+"\\"+fileName+".png");
        imageOutFile.write(imageByteArray);
        imageOutFile.close();
        System.out.println("SUCCESSFULLY SAVED IMAGE");
*/
      //  return "homepage";
   //}
    @GetMapping("/download")
    public String download(Model model) throws IOException, JSONException {

        return "homepage";
    }
    @GetMapping("/viewFriends")
    public String viewFriends(Model model) throws IOException, JSONException {
        //model.addAttribute("greeting", new Greeting());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","viewFriends");
        jsonObject.put("email",userAttributes.get("email"));
        serverAccess.sendJSON(jsonObject);
        JSONObject friendList = serverAccess.recieveMsg();
        System.out.println("YOU HAVE " +friendList.length() + "FRIENDS");
        String[] friendArray = new String[friendList.length()];
        for(int x=0;x<friendList.length();x++){
            friendArray[x]= friendList.getString("email"+String.valueOf(x+1));
        }
        for(int x=0;x<friendArray.length;x++){
            System.out.println(friendArray[x]+"!!!!");
        }
        model.addAttribute("friendArray", friendArray);
        model.addAttribute("name", userAttributes.get("name"));
        return "viewFriends";
    }
    @GetMapping("/register")
    public String register(Model model) {
        //model.addAttribute("greeting", new Greeting());
        model.addAttribute("name", userAttributes.get("name"));
        model.addAttribute("email", userAttributes.get("email"));
        return "register";
    }
    @PostMapping ("/idkfunc")
    public String idkfunc(Model model,@ModelAttribute("friendForm") imageMsg imageMsg) {
        System.out.println("DSAUIDIBJNHSAIBJDHSABHJDSA"+imageMsg.fromEmail);
        return "homepage";
    }
    @PostMapping ("/register")
    public RedirectView registerInDB(Model model,@RequestParam  String filePath) throws JSONException, IOException {
        //model.addAttribute("greeting", new Greeting());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type","insert");
        jsonObject.put("table","user");
        jsonObject.put("email",userAttributes.get("email"));
        jsonObject.put("path",filePath);
        serverAccess.sendJSON(jsonObject);
        JSONObject returnJSON = serverAccess.recieveMsg();
        System.out.println("REGISTERING WITH PATH "+ filePath);
        return new RedirectView("/homepage");
    }
    @GetMapping("/encode")
    public String encode(@RequestParam(name="user", required=false, defaultValue="defaultUser") String name,Model model) throws IOException {


        model.addAttribute("user", name);
        model.addAttribute("email", userAttributes.get("email"));
        model.addAttribute("msg", msg);
        /*System.out.println("MESSAGE IS " +msg);
        model.addAttribute("user", name);
        model.addAttribute("email", userAttributes.get("email"));
        model.addAttribute("msg", msg);
        System.out.println("HOWDIE");
        System.out.println(Desktop.isDesktopSupported()  );
        String chosenFile = "C:\\Users\\mcrossley\\IdeaProjects\\Stego Project Oauth\\demo\\src\\main\\java\\com\\example\\demo\\cat.png";
        chosenFile="C:\\Users\\mcrossley\\Desktop\\temp\\chair.png";
        String message = "Somebody once told me the world is gonna roll me I ain't the sharpest tool in the shed She was looking kind of dumb with her finger and her thumb";
            message= "THIS IF FINNALLY WORKING POG";
        int length = message.length();
        String binLength = Integer.toString(length,2);
        image theImage = new image(chosenFile);
        theImage.readImage();
        theImage.readPixels();
        theImage.hidemsgLength(binLength);
        theImage.hideData(message);
        theImage.getMsgLength();
        theImage.saveNewImage("C:\\Users\\mcrossley\\IdeaProjects\\Stego Project Oauth\\demo\\src\\main\\java\\com\\example\\demo\\new IMAGE.png");
        theImage.findCentre();
        //return "test";
        */

        return "encode";
    }
    @PostMapping("/encode")
    public String encodeImagePost(Model model,@RequestParam String fileName,@RequestParam String msg,@RequestParam String newFileName) throws JSONException, IOException {
        System.out.println("ENCODING " + filePath + "  " + fileName+ " with " + msg + "  fsa"+newFileName);
        int length = msg.length();

        String binLength = Integer.toString(length,2);

        image theImage = new image(filePath+"\\"+fileName);
        theImage.readImage();
        theImage.readPixels();
        theImage.hidemsgLength(binLength);
        theImage.hideData(msg);
      //  theImage.getMsgLength2();
       //theImage.extractData2();
        //theImage.hideData(msg);
        imageCentre center = theImage.findCentre();
        theImage.getMsgLength(center);

        theImage.saveNewImage(filePath+"\\"+newFileName+".png");
        //theImage.findCentre();
        model.addAttribute("status","success");
        msg = "'"+msg+"'";
        model.addAttribute("msg",msg);
        newFileName=newFileName+".png";
        model.addAttribute("fileName",newFileName);
        return "encode";
    }
    @GetMapping("/decode")
    public String decode(@RequestParam(name="user", required=false, defaultValue="defaultUser") String name, Model model) throws IOException {
   /* public String decode(@RequestParam(name="user", required=false, defaultValue="defaultUser") String name, Model model) throws IOException {*/


        model.addAttribute("user", name);
        model.addAttribute("email", userAttributes.get("email"));
       // image theImage = new image("C:\\Users\\mcrossley\\IdeaProjects\\Stego Project Oauth\\demo\\src\\main\\java\\com\\example\\demo\\Output.png");
        //image theImage = new image(filePath);

       // theImage.readImage();
       // theImage.findCentre();
        //int length = theImage.getMsgLength();
        //System.out.println("IMAGE LENGTH IS  " + length);
        //System.out.println("IMAGE MESSAGE IS  " + theImage.extractData());
       // System.out.println("DECODING");
        return "decode";
    }
    @PostMapping("/decode")
    public String decodeImage(Model model,@RequestParam String fileName) throws JSONException, IOException {
        System.out.println("DECDING " + filePath + "  " + fileName);
        image theImage = new image(filePath+"\\"+fileName);
        theImage.readImage();
       // imageCentre center = theImage.findCentre();
        imageCentre center = theImage.isCentre();
        if(center==null){
            model.addAttribute("msg","error couldnt find any message");
        }else{
            int length = theImage.getMsgLength(center);
            System.out.println("IMAGE LENGTH IS  " + length);
            System.out.println("IMAGE MESSAGE IS  " + theImage.extractData(center));
            String msg = theImage.extractData(center);
            model.addAttribute("msg",msg);
        }

        model.addAttribute("fileName",fileName);
        return "decode";
    }
    @GetMapping("/homepage")
    public String test(@RequestParam(name="user", required=false, defaultValue="defaultUser") String name, Model model) throws IOException, JSONException {
        model.addAttribute("name", userAttributes.get("name"));
        model.addAttribute("email", userAttributes.get("email"));
       // image theImage = new image("C:\\Users\\mcrossley\\IdeaProjects\\Stego Project Oauth\\demo\\src\\main\\java\\com\\example\\demo\\Output.png");
       // theImage.readImage();
       // theImage.findCentre();
       // System.out.println("howdieeee");

       // serverAccess.sendMsg("HOMEPAGE " + userAttributes.get("name"));

        JSONObject json = new JSONObject();
        json.put("type","login");
        json.put("email",userAttributes.get("email"));

        System.out.println(json.toString());
     //   serverAccess.sendMsg(json.toString());
        return "homepage";
    }


    @GetMapping("/")
    public String homepage() {
        return "homepage";
    }

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;
    @GetMapping("/loginSuccess")
    public RedirectView getLoginInfo(Model model, OAuth2AuthenticationToken authentication) throws IOException, JSONException {
        OAuth2AuthorizedClient client = authorizedClientService
                .loadAuthorizedClient(
                        authentication.getAuthorizedClientRegistrationId(),
                        authentication.getName());
        String userInfoEndpointUri = client.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUri();

        if (!StringUtils.isEmpty(userInfoEndpointUri)) {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken()
                    .getTokenValue());
            HttpEntity entity = new HttpEntity("", headers);
            ResponseEntity<Map> response = restTemplate
                    .exchange(userInfoEndpointUri, HttpMethod.GET, entity, Map.class);
            userAttributes = response.getBody();
            model.addAttribute("name", userAttributes.get("name"));
        //Besides the name, the userAttributes Map also contains properties such as email, family_name, picture, locale.
        }



        serverAccess = new ChatClient();
        int port =1777 ;
        String address = "localhost";
       // address="https://mcrossleytest.azurewebsites.net";
        JSONObject json = new JSONObject();
        json.put("type","login");
        json.put("email",userAttributes.get("email"));
        serverAccess.SetPortAddress(address, port);
        System.out.println(json.toString());
        serverAccess.sendJSON(json);
        JSONObject reply = serverAccess.recieveMsg();
        if(reply.getBoolean("isRegistered")==false){
            System.out.println("YOU ARE NEW USER");
            return new RedirectView("/register");
        }else{
            System.out.println("WELCOME BACK");
            filePath= reply.getString("filePath");
            System.out.println("FILE PATH IS "+filePath);
            return new RedirectView("/homepage");
           /* return new RedirectView("http://localhost:8080/register");*/
        }

        //client.thread1.start();
        //client.ConsoleGo();

       // System.out.println("Recieved from Server: " + jsonObject.toString(2));
       // System.out.println(jsonObject.get("message"));


       // return new RedirectView("http://localhost:8080/homepage");
        //model.addAttribute("attribute", "redirectWithRedirectPrefix");
        //return new ModelAndView("redirect:/redirectedUrl", model);
        //return "homepage";
    }



    public static byte[] decodeImage(String imageDataString) {
        return Base64.getDecoder().decode(imageDataString);
    }

}
