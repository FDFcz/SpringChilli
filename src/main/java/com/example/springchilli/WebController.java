package com.example.springchilli;

import com.example.Controlers.AuthorizationControler;
import com.example.Controlers.ChiliPeperApplication;
import com.example.Structures.Cron;
import com.example.Structures.Customer;
import com.example.Structures.Schedule;
import com.example.Structures.Teracota;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Random;

@Controller
public class WebController {

    //private final HttpServletResponse httpServletResponse;
    private final AuthorizationControler authorizationControler;
    private Random rnd;
    //@Autowired
    public WebController(ChiliPeperApplication chiliPeperApplication, HttpServletResponse httpServletResponse) {
        authorizationControler = new AuthorizationControler();
        //this.chiliPeperApplication = chiliPeperApplication;
        //this.httpServletResponse = httpServletResponse;
        rnd = new Random(new Date().getTime());
    }
    public WebController() {
        authorizationControler = new AuthorizationControler();
        rnd = new Random(new Date().getTime());
    }
    /*
    @RequestMapping("/**")
    public String notFound(HttpServletRequest request) {
        String requestURL = request.getRequestURL().toString();
        // Optionally, include the query string if present
        String queryString = request.getQueryString();

        // Print the full URL with the query string (if available)
        System.out.println("Requested URL: " + requestURL + (queryString != null ? "?" + queryString : ""));

        return "404";
    }

     */

    @GetMapping("/test")
    public String testwiev(){
        return "test2";
    }

    @GetMapping("/")
    public String toHome(){
        return "about/index";
        //return "test";
    }
    @GetMapping("/403")
    public String to403(){
        return "403";
    }
    @GetMapping("/ViewTables")
    //@ResponseBody
    public String ViewTables(){
        return "ViewTables";
    }
//region [customer]
    @GetMapping("/registry")
    public String toRegistry(){return "registry";}
    @PostMapping("/registry")
    public String toRegistryAgain(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String password2,
            Model model)
    {
        model.addAttribute("ERROR",1);
        if(ChiliPeperApplication.getUserID(username)!=-1) return "registry";
        if(!password.equals(password2)||password.equals("")) return "registry";
        ChiliPeperApplication.registryNewUser(username, password);
        return "redirect:";
    }
    @GetMapping("/login")
    public String tryToLogging(){
        return "login";
    }
    @PostMapping("/login")
    public String tryToLogging(
            HttpServletResponse response,
            @RequestParam String username,
            @RequestParam String password,
            Model model
    ){
        String newToken = username+"?"+ rnd.nextInt(Integer.MAX_VALUE);
        int userID=authorizationControler.LogUser(username,password,newToken);
        if(userID >-1)
        {
            Cookie cookie = new Cookie("UserToken",newToken);
            cookie.setMaxAge(3600); //1h
            response.addCookie(cookie);
            return "redirect:userHome?id="+userID;
        }
        model.addAttribute("ERROR",1);
        return "login";
    }
    @GetMapping("/logOut")
    public String ToLogOut(@RequestParam String id)
    {
        authorizationControler.LogOutUser(Integer.valueOf(id));
        return "redirect:/";
    }
    @GetMapping("ToUserHome")
    public String toUserHome(@CookieValue(value = "UserToken",required = false) String userToken, Model model)
    {
        if(userToken==null) return "redirect:/login";
        String userName="";
        for (int i =0;i<userToken.length();i++)
        {
            if(userToken.charAt(i)=='?') break;
            userName+=userToken.charAt(i);
        }
        int id = ChiliPeperApplication.getUserID(userName);
        if(id==-1) return "redirect:/login";

        return "redirect:userHome?id="+id;
        //return "user/userHome";
    }
    @GetMapping("/userHome")
    public String userHome(@RequestParam String id,
            @CookieValue("UserToken") String userToken,Model model)
    {
        int _id = Integer.valueOf(id);
        if(authorizationControler.isCustomerAuthorize(_id,userToken))
        {
            model.addAttribute("userName",  ChiliPeperApplication.getUser(_id).getUserName());
            model.addAttribute("userID", id);
            List<Teracota> terracotaList = ChiliPeperApplication.getUserWithTeracotas(_id).getOwnedTeracotas();
            model.addAttribute("terracotaList", terracotaList);  //
            return "user/userHome";
        }
        return "redirect:login";
    }
    @GetMapping( "/ChangePassword")
    public String toChangePassword( @RequestParam String id,
                                    @CookieValue("UserToken") String userToken)
    {
        if(!authorizationControler.isCustomerAuthorize(Integer.valueOf(id),userToken)) return "redirect:403";
        return "user/changePassword";
    }
    @PostMapping( "/ChangePassword")
    public String ChangePassword( @RequestParam String id,
                                  @RequestParam String oldPassword,
                                  @RequestParam String password,
                                  @RequestParam String password2,
                                  @CookieValue("UserToken") String userToken,
            Model model)
    {
        model.addAttribute("ERROR",1);
        if(!authorizationControler.isCustomerAuthorize(Integer.valueOf(id),userToken)) return "redirect:login";
        if(!password.equals(password2)||password.equals("")) return "user/changePassword";
        if(!authorizationControler.changePassword(Integer.valueOf(id),oldPassword,password)) return "changePassword";
        return "redirect:login";
    }
    //endregion

//region [teracota]
    @GetMapping( value = "/newTeracota")
    public String newTeracota(@RequestParam String id,
                              @CookieValue("UserToken") String userToken,
    Model mode)
    {
        if(!authorizationControler.isCustomerAuthorize(Integer.valueOf(id),userToken)) return "redirect:login";
        mode.addAttribute("id",id);
        return "user/newTeracota";
    }

    @PostMapping("/newTeracota")
    public String addTeracota(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String plantType,
            @CookieValue("UserToken") String userToken)
    {
        System.out.println(id);
        if(!authorizationControler.isCustomerAuthorize(Integer.valueOf(id),userToken)) return "redirect:login";
        Teracota newTeracota = new Teracota(name, Teracota.PlantTypes.valueOf(plantType));
        Customer currentUser = ChiliPeperApplication.getUser(Integer.valueOf(id));
        ChiliPeperApplication.addTeracota(currentUser,newTeracota);
        return "redirect:userHome?id="+id;
    }
    @GetMapping( value = "/teracotaDetail")
    public String detailTeracota(@RequestParam (required = false) String id,
                                 @RequestParam (required = false) String teracota,
                                 @RequestParam (required = false) String method,
                                 @CookieValue("UserToken") String userToken,
            Model model)
    {

        if(!authorizationControler.isCustomerAuthorize(Integer.valueOf(id),userToken)) return "redirect:login";
        if(!authorizationControler.isCustomerOwnerOfTeracota(Integer.valueOf(id),Integer.valueOf(teracota))) return "redirect:403";
        Teracota currentTeracota = ChiliPeperApplication.getTeracota(Integer.valueOf(teracota));
        List<Cron> crons = ChiliPeperApplication.getCronsForTeracota(currentTeracota.getId());
        model.addAttribute("currentTeracota", currentTeracota);
        model.addAttribute("crons", crons);
        model.addAttribute("userID", id);
        if (method == null) return "user/teracotaDetail";
        if(method.equals("Delete"))
        {
            ChiliPeperApplication.deleteTeracota(Integer.valueOf(teracota));
            return "redirect:userHome?id="+id;
        }
        return "teracotaDetail";
    }
    @PostMapping("/teracotaDetail")
    public String ChangeTeracota(@RequestParam String id,
                                 @RequestParam String teracota,
                                 @RequestParam(required = false, value="start") String[] startsTime,
                                 @RequestParam(required = false, value="end") String[] endsTime,
                                 @RequestParam(required = false, value="temp") String[] temps,
                                 @RequestParam(required = false, value="humidity") String[] humidities,
                                 @RequestParam(required = false, value="light") String[] lights,
                                 @RequestParam(required = false, value="cronID") String[] cronsID,
                                 @RequestParam(required = false, value="schedlID") String[] scheduleID,
                                 @CookieValue("UserToken") String userToken)
    {
        if(!authorizationControler.isCustomerAuthorize(Integer.valueOf(id),userToken)) return "redirect:403";
        if(!authorizationControler.isCustomerOwnerOfTeracota(Integer.valueOf(id),Integer.valueOf(teracota))) return "redirect:403";
        if (startsTime==null) return "redirect:teracotaDetail?id="+id+"&&teracota="+teracota;
        else //save crons
        {
            for (int i=0;i<startsTime.length;i++)
            {
                Schedule updateSchedl = new Schedule(Integer.valueOf(scheduleID[i]),Float.valueOf(temps[i]),(Integer.valueOf(lights[i])>0),Integer.valueOf(humidities[i]));
                Cron cron = new Cron(Integer.valueOf(cronsID[i]),updateSchedl,Integer.valueOf(startsTime[i]),Integer.valueOf(endsTime[i]));
                ChiliPeperApplication.updateCron(cron);
            }
            return "redirect:userHome?id="+id;
        }
    }
    @GetMapping("/teracotaGallery")
    public String Gallery(@RequestParam String id,
                             @RequestParam String teracota,
                             @CookieValue("UserToken") String userToken)
    {
        if(!authorizationControler.isCustomerAuthorize(Integer.valueOf(id),userToken)) return "redirect:403";
        return "user/gallery";
    }
    @GetMapping("/ChangeCronNumber")
    public String ChangeCron(@RequestParam String id,
                             @RequestParam String teracota,
                             @RequestParam String cronID,
                             @RequestParam (required = false) String method,
                             @CookieValue("UserToken") String userToken)
    {
        if(!authorizationControler.isCustomerAuthorize(Integer.valueOf(id),userToken)) return "redirect:403";
        if (method == null)
        {
            return "ChangeCron";
        }
        if(method.equals("Delete"))
        {
            ChiliPeperApplication.deleteCron(Integer.valueOf(cronID));
            return "redirect:teracotaDetail?id="+id+"&&teracota="+teracota;
        }
        if (method.equals("Post"))
        {
            ChiliPeperApplication.addNewCron(Integer.valueOf(teracota));
            return "redirect:teracotaDetail?id="+id+"&&teracota="+teracota;
        }

        return "redirect:userHome?id="+id;
    }
    //endregion
    @GetMapping("/about")
    public String about()
    {
        return "about/about";
    }
    @GetMapping("/aboutChylli")
    public String aboutChilly()
    {
        return "about/chilly";
    }
    @GetMapping("/priceList")
    public String priceList()
    {
        return "about/priceList";
    }
}