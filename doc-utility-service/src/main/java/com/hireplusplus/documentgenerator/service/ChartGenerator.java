package com.hireplusplus.documentgenerator.service;

import com.hireplusplus.documentgenerator.models.api.RadarData;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

@Service
public class ChartGenerator {

    HashMap<String,String> chartMap = new HashMap<>();

    public String getChartHtmlString(String id) {
        return chartMap.get(id);
    }
    @Autowired
    private TemplateEngine templateEngine;
    public String generateChartUsingThymeleafAndChartJs(RadarData radarData){
//        radarData.setLabel("skillPointer");
//        radarData.setType("radar");
        Context testContext = new Context();
        testContext.setVariable("name","Akhil");
        testContext.setVariable("rating","2.5");
        testContext.setVariable("radarData",radarData);
        String html=templateEngine.process("../test/test",testContext);
        String id = UUID.randomUUID().toString();
        chartMap.put(id,html);
        try{
//            OutputStream outputStream = new FileOutputStream("report.pdf");
//            Document document = Jsoup.parse(html);
//            document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
//            String xhtml= document.html();
//            System.out.println(xhtml);
//            ITextRenderer renderer = new ITextRenderer();
//            renderer.setDocumentFromString(xhtml);
//            renderer.layout();
//            renderer.createPDF(outputStream);
//            outputStream.close();
//            System.out.println("sample report done");
        }catch (Exception e){
            e.printStackTrace();
            return "pdf generation failure";
        }
        return id;
    }


    public String jFreeChartRadar(){
        try{

            String s = "Skill";
//            String s1 = "Second";
//            String s2 = "Third";
            String s3 = "Java";
            String s4 = "Spring boot";
            String s5 = "Javascript";
            String s6 = "Angular";
            String s7 = "Mongodb";
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            dataset.addValue(5D, s, s3);
            dataset.addValue(5D, s, s4);
            dataset.addValue(5D, s, s5);
            dataset.addValue(5D, s, s6);
            dataset.addValue(5D, s, s7);
//            dataset.addValue(5D, s1, s3);
//            dataset.addValue(7D, s1, s4);
//            dataset.addValue(6D, s1, s5);
//            dataset.addValue(8D, s1, s6);
//            dataset.addValue(4D, s1, s7);
//            dataset.addValue(4D, s2, s3);
//            dataset.addValue(3D, s2, s4);
//            dataset.addValue(2D, s2, s5);
//            dataset.addValue(3D, s2, s6);
//            dataset.addValue(6D, s2, s7);
            SpiderWebPlot plot = new SpiderWebPlot(dataset);
            plot.setOutlineVisible(true);
//            plot.setInteriorGap(0.40000000000000002D);
//            plot.setToolTipGenerator(new StandardCategoryToolTipGenerator());
            JFreeChart radar = new JFreeChart("Radar Chart", TextTitle.DEFAULT_FONT, plot, false);

            ChartUtils.saveChartAsPNG(new File("radar.png"), radar, 1000, 1000);


        }catch (Exception e){
            e.printStackTrace();
            return "Failure";
        }
        return "success";
    }



        public String jfreeChartHistoGram(){
        try {
            double[] vals = {

                    0.71477137, 0.55749811, 0.50809619, 0.47027228, 0.25281568,
                    0.66633175, 0.50676332, 0.6007552, 0.56892904, 0.49553407,
                    0.61093935, 0.65057417, 0.40095626, 0.45969447, 0.51087888,
                    0.52894806, 0.49397198, 0.4267163, 0.54091298, 0.34545257,
                    0.58548892, 0.3137885, 0.63521146, 0.57541744, 0.59862265,
                    0.66261386, 0.56744017, 0.42548488, 0.40841345, 0.47393027,
                    0.60882106, 0.45961208, 0.43371424, 0.40876484, 0.64367337,
                    0.54092033, 0.34240811, 0.44048106, 0.48874236, 0.68300902,
                    0.33563968, 0.58328107, 0.58054283, 0.64710522, 0.37801285,
                    0.36748982, 0.44386445, 0.47245989, 0.297599, 0.50295541,
                    0.39785732, 0.51370486, 0.46650358, 0.5623638, 0.4446957,
                    0.52949791, 0.54611411, 0.41020067, 0.61644868, 0.47493691,
                    0.50611458, 0.42518211, 0.45467712, 0.52438467, 0.724529,
                    0.59749142, 0.45940223, 0.53099928, 0.65159718, 0.38038268,
                    0.51639554, 0.41847437, 0.46022878, 0.57326103, 0.44913632,
                    0.61043611, 0.42694949, 0.43997814, 0.58787928, 0.36252603,
                    0.50937634, 0.47444256, 0.57992527, 0.29381335, 0.50357977,
                    0.42469464, 0.53049697, 0.7163579, 0.39741694, 0.41980533,
                    0.68091159, 0.69330702, 0.50518926, 0.55884098, 0.48618324,
                    0.48469854, 0.55342267, 0.67159111, 0.62352006, 0.34773486};


            HistogramDataset dataset = new HistogramDataset();
            dataset.addSeries("key", vals, 50);
            JFreeChart histogram = ChartFactory.createHistogram("Normal distribution",
                    "y values", "x values", dataset);
            ChartUtils.saveChartAsPNG(new File("histogram.png"), histogram, 450, 400);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "success";
    }




//    public static void generatePdfFromHtml(String html) {
//        try {
//            html = "<!DOCTYPE html>\n" +
//                    "<html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
//                    "\n" +
//                    "<head>\n" +
//                    "    <meta charset=\"UTF-8\"></meta>\n" +
//                    "    <meta name=\"viewport\"\n" +
//                    "          content=\"width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0\"></meta>\n" +
//                    "    <meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\"></meta>\n" +
//                    "    <!--    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css\" rel=\"stylesheet\">-->\n" +
//                    "    <!--    <script src=\"https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js\"></script>-->\n" +
//                    "    <!--     <link th:rel=\"stylesheet\" type=\"text/css\" media=\"all\" href=\"css/bootstrap.min.css\"  th:href=\"@{css/bootstrap.min.css} \"/>-->\n" +
//                    "    <!--    <link th:rel=\"stylesheet\" th:href=\"@{webjars/bootstrap/5.1.3/css/bootstrap.min.css} \"/>-->\n" +
//                    "\n" +
//                    "    <title>Account Confirmation</title>\n" +
//                    "\n" +
//                    "    <style>\n" +
//                    "        .float-parent{\n" +
//                    "            width: 100%;\n" +
//                    "            height:200px;\n" +
//                    "\n" +
//                    "        }\n" +
//                    "        .col-float-left{\n" +
//                    "            width: 50%;\n" +
//                    "            float: left;\n" +
//                    "        }\n" +
//                    "        .col-float-right{\n" +
//                    "            width: 50%;\n" +
//                    "            float: right;\n" +
//                    "        }\n" +
//                    "\n" +
//                    "\n" +
//                    "\n" +
//                    "        .btn-primary {\n" +
//                    "            color: #fff;\n" +
//                    "            background-color: #0d6efd;\n" +
//                    "            border-color: #0d6efd;\n" +
//                    "\n" +
//                    "        }\n" +
//                    "\n" +
//                    "        .btn-primary:hover {\n" +
//                    "            color: #fff;\n" +
//                    "            background-color: #0b5ed7;\n" +
//                    "            border-color: #0a58ca;\n" +
//                    "            cursor: pointer;\n" +
//                    "        }\n" +
//                    "\n" +
//                    "        .btn {\n" +
//                    "\n" +
//                    "            display: inline-block;\n" +
//                    "            font-weight: 400;\n" +
//                    "            line-height: 1.5;\n" +
//                    "            text-align: center;\n" +
//                    "            text-decoration: none;\n" +
//                    "            vertical-align: middle;\n" +
//                    "            -webkit-user-select: none;\n" +
//                    "            -moz-user-select: none;\n" +
//                    "            user-select: none;\n" +
//                    "            padding: 0.375rem 0.75rem;\n" +
//                    "            font-size: 1rem;\n" +
//                    "            border-radius: 0.25rem;\n" +
//                    "            transition: color .15s ease-in-out, background-color .15s ease-in-out, border-color .15s ease-in-out, box-shadow .15s ease-in-out;\n" +
//                    "\n" +
//                    "        }\n" +
//                    "\n" +
//                    ".outer-wrapper {\n" +
//                    "            /* width: 854px;\n" +
//                    "            height: 821px; */\n" +
//                    "            /* width: 70%;\n" +
//                    "            height: 60%; */\n" +
//                    "            width: 70%;\n" +
//                    "            background-image: url(https://i.postimg.cc/9Msq9rhL/waves.png), linear-gradient(#206dc5 30%, white 0%);\n" +
//                    "            padding: 7%;\n" +
//                    "            background-size:contain;\n" +
//                    "\n" +
//                    "            /* background: linear-gradient(#206DC5 30%, white 0%); */\n" +
//                    "            /* background: #206DC5; */\n" +
//                    "            /* box-shadow: 5px 10px #888888; */\n" +
//                    "\n" +
//                    "        }\n" +
//                    "\n" +
//                    "        .inner-wrapper {\n" +
//                    "            box-sizing: border-box;\n" +
//                    "            /* width: 707px;\n" +
//                    "            height: 640px; */\n" +
//                    "            /* width: 70%;\n" +
//                    "            height: 60%; */\n" +
//                    "            /* background: #FFFFFF; */\n" +
//                    "            /* border: 1px solid #103662; */\n" +
//                    "            margin: auto;\n" +
//                    "            padding: 7%;\n" +
//                    "            background: #F9FAFB;\n" +
//                    "\n" +
//                    "            box-shadow: 0px 1px 2px rgba(16, 24, 40, 0.05);\n" +
//                    "        }\n" +
//                    "\n" +
//                    "        .inner-box {\n" +
//                    "            background: #EAF3FE;\n" +
//                    "            padding: 5%;\n" +
//                    "        }\n" +
//                    "\n" +
//                    "        .logo-header {\n" +
//                    "            width: 100px;\n" +
//                    "            padding-left: 0%;\n" +
//                    "            padding-bottom: 3%;\n" +
//                    "        }\n" +
//                    "\n" +
//                    "        .info-title {\n" +
//                    "            /* width: 347px;\n" +
//                    "            height: 61px; */\n" +
//                    "\n" +
//                    "            font-family: 'Inter';\n" +
//                    "            font-style: normal;\n" +
//                    "            font-weight: 600;\n" +
//                    "            font-size: 48px;\n" +
//                    "            line-height: 60px;\n" +
//                    "            /* or 125% */\n" +
//                    "\n" +
//                    "            letter-spacing: 0.02em;\n" +
//                    "\n" +
//                    "            color: #344054;\n" +
//                    "            overflow: hidden;\n" +
//                    "\n" +
//                    "        }\n" +
//                    "\n" +
//                    "        .info-title-subscript {\n" +
//                    "            /* width: 246px;\n" +
//                    "            height: 60px; */\n" +
//                    "\n" +
//                    "            font-family: 'Inter';\n" +
//                    "            font-style: normal;\n" +
//                    "            font-weight: 600;\n" +
//                    "            font-size: 21px;\n" +
//                    "            line-height: 60px;\n" +
//                    "            /* identical to box height, or 286% */\n" +
//                    "\n" +
//                    "            letter-spacing: -0.02em;\n" +
//                    "\n" +
//                    "            color: #667085;\n" +
//                    "        }\n" +
//                    "\n" +
//                    "        .info-illustration {\n" +
//                    "            /* padding: 5%; */\n" +
//                    "            text-align: center;\n" +
//                    "            overflow: hidden;\n" +
//                    "            /* display: flex;\n" +
//                    "            flex-direction: column-reverse; */\n" +
//                    "\n" +
//                    "            /* background-image: url(img/illustartion.svg); */\n" +
//                    "            /* background: #185294; */\n" +
//                    "\n" +
//                    "            /* width: 198.66px;\n" +
//                    "            height: 176.15px; */\n" +
//                    "\n" +
//                    "            /* background: #185294; */\n" +
//                    "        }\n" +
//                    "\n" +
//                    "        /* .info-illustration img{\n" +
//                    "            width: 110%;\n" +
//                    "        } */\n" +
//                    "\n" +
//                    "\n" +
//                    "\n" +
//                    "        .info-main-body {\n" +
//                    "            /* width: 366px;\n" +
//                    "            height: 48px; */\n" +
//                    "\n" +
//                    "            font-family: 'Inter';\n" +
//                    "            font-style: normal;\n" +
//                    "            font-weight: 400;\n" +
//                    "            font-size: 16px;\n" +
//                    "            line-height: 20px;\n" +
//                    "            /* or 125% */\n" +
//                    "\n" +
//                    "\n" +
//                    "            /* Gray/900 */\n" +
//                    "\n" +
//                    "            color: #101828;\n" +
//                    "        }\n" +
//                    "\n" +
//                    "        .info-footer-body {\n" +
//                    "            /* width: 634px;\n" +
//                    "            height: 94px; */\n" +
//                    "\n" +
//                    "            font-family: 'Inter';\n" +
//                    "            font-style: normal;\n" +
//                    "            font-weight: 400;\n" +
//                    "            font-size: 14px;\n" +
//                    "            line-height: 20px;\n" +
//                    "            /* or 143% */\n" +
//                    "\n" +
//                    "\n" +
//                    "            /* Gray/800 */\n" +
//                    "\n" +
//                    "            color: #1D2939;\n" +
//                    "            margin-top: 5%;\n" +
//                    "\n" +
//                    "        }\n" +
//                    "\n" +
//                    "        .footer {\n" +
//                    "            /* width: 440px;\n" +
//                    "            height: 24px; */\n" +
//                    "\n" +
//                    "            /* Text sm/Normal */\n" +
//                    "\n" +
//                    "            font-family: 'Inter';\n" +
//                    "            font-style: normal;\n" +
//                    "            font-weight: 400;\n" +
//                    "            font-size: 14px;\n" +
//                    "            line-height: 20px;\n" +
//                    "\n" +
//                    "            /* or 143% */\n" +
//                    "\n" +
//                    "\n" +
//                    "            /* Gray/800 */\n" +
//                    "\n" +
//                    "            color: #1D2939;\n" +
//                    "        }\n" +
//                    "\n" +
//                    "        @media (max-width: 768px) {\n" +
//                    "            .outer-wrapper {\n" +
//                    "                text-align: center;\n" +
//                    "                min-width: 100px;\n" +
//                    "                width: auto;\n" +
//                    "\n" +
//                    "            }\n" +
//                    "             .float-parent{\n" +
//                    "            width: 100%;\n" +
//                    "            height:420px\n" +
//                    "\n" +
//                    "        }\n" +
//                    "        .col-float-left{\n" +
//                    "            width: 100%;\n" +
//                    "        }\n" +
//                    "        .col-float-right{\n" +
//                    "                    width: 100%;\n" +
//                    "\n" +
//                    "        }\n" +
//                    "\n" +
//                    "            .info-title {\n" +
//                    "                font-family: 'Inter';\n" +
//                    "                font-style: normal;\n" +
//                    "                font-weight: 600;\n" +
//                    "                font-size: 40px;\n" +
//                    "                line-height: 50px;\n" +
//                    "                /* or 125% */\n" +
//                    "\n" +
//                    "                letter-spacing: 0.02em;\n" +
//                    "\n" +
//                    "                /* Gray/700 */\n" +
//                    "\n" +
//                    "                color: #344054;\n" +
//                    "                word-wrap: break-word;\n" +
//                    "            }\n" +
//                    "            .info-title-subscript {\n" +
//                    "            /* width: 246px;\n" +
//                    "            height: 60px; */\n" +
//                    "\n" +
//                    "            font-family: 'Inter';\n" +
//                    "            font-style: normal;\n" +
//                    "            font-weight: 600;\n" +
//                    "            font-size: 21px;\n" +
//                    "            line-height: 50px;\n" +
//                    "            /* identical to box height, or 286% */\n" +
//                    "\n" +
//                    "            letter-spacing: -0.02em;\n" +
//                    "\n" +
//                    "            color: #667085;\n" +
//                    "        }\n" +
//                    "        }\n" +
//                    "\n" +
//                    "    </style>\n" +
//                    "</head>\n" +
//                    "\n" +
//                    "<body>\n" +
//                    "<div class=\"outer-wrapper container-md\">\n" +
//                    "    <div class=\"inner-wrapper container-md \">\n" +
//                    "        <img class=\"logo-header container-md\" alt=\"HirePlusPlus Logo\" src=\"cid:logo\"></img>\n" +
//                    "        <div class=\"inner-box\">\n" +
//                    "\n" +
//                    "        <div class=\"info-title container-md\">\n" +
//                    "            Hi,<b>Akhil</b>\n" +
//                    "        </div>\n" +
//                    "        <div class=\"info-title-subscript container-md\">Thank you for registering</div>\n" +
//                    "\n" +
//                    "        <div class=\"row float-parent\">\n" +
//                    "            <div class=\"info-illustration col-float-right \">\n" +
//                    "                <img alt=\"welcome illustration\" src=\"pattern.jpeg\"></img>\n" +
//                    "            </div>\n" +
//                    "            <div class=\"container col-float-left\">\n" +
//                    "                <div class=\"info-main-body\">\n" +
//                    "                    <p> Click the below button to confirm your email and finish creating your Hire++ account</p>\n" +
//                    "                    <p>This link will expire in 2 hours and can only be used once.</p>\n" +
//                    "                    <a href=\"verificationUrl\">\n" +
//                    "                        <button type=\"button\" class=\"btn btn-primary\">Activate Account</button>\n" +
//                    "                    </a>\n" +
//                    "                </div>\n" +
//                    "            </div>\n" +
//                    "\n" +
//                    "        </div>\n" +
//                    "            </div>\n" +
//                    "\n" +
//                    "\n" +
//                    "\n" +
//                    "            <div class=\"info-footer-body \">\n" +
//                    "            <p>Can't click the button above? Copy and paste this link into your browser .</p>\n" +
//                    "            <p style=\"text-decoration: underline;\"><b>verificationUrl</b></p>\n" +
//                    "        </div>\n" +
//                    "        <div class=\"footer \">\n" +
//                    "            If you did not make this request, you can safely ignore this email.\n" +
//                    "        </div>\n" +
//                    "    </div>\n" +
//                    "</div>\n" +
//                    "</body>\n" +
//                    "\n" +
//                    "</html>";
//            OutputStream outputStream = new FileOutputStream("report.pdf");
//            ITextRenderer renderer = new ITextRenderer();
//            renderer.setDocumentFromString(html);
//            renderer.layout();
//            renderer.createPDF(outputStream);
//            outputStream.close();
//            System.out.println("sample report done");
//            System.out.println(html);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//    }
}
