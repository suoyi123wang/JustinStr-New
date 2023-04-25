package cn.ios.report;
import cn.ios.casegen.config.GlobalCons;
import cn.ios.casegen.util.FileUtil;
import cn.ios.casegen.util.StringUtil;
import cn.ios.report.service.SearchService;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;

/**
 * generate static html
 * @author: wangmiaomiao
 * @create: 2022/10/19 18:57
 **/
public class ReportFactory {
    public static void genReport(){
        try {
            FileUtils.deleteDirectory(new File(GlobalCons.TEST_COMPILE_TEMP_FOLDER));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Reports are generated only if there are generated test cases
        boolean hasTestCase = FileUtil.hasFile(GlobalCons.TEST_OUTPUT_FOLDER);
        if (!hasTestCase) {
            System.exit(0);
            return;
        }

        // compute generation time
        if (GlobalCons.START_TIME == 0L) {
            System.err.println("cannot obtain start time, so cannot compute generation time");
        } else {
            long genTime = (System.currentTimeMillis() - GlobalCons.START_TIME) / 1000;
            GlobalCons.EXECUTE_TIME = StringUtil.formatString(String.valueOf(genTime)) + "s";
        }

        List<Map<String, String>> classInfo = SearchService.getClassInfo1();
        Map<String, List<String>> exceptionInfo = SearchService.getExceptionInfo1("", "");
        Map<String, List<String>> detailInfo = SearchService.getDetailInfo1("", "");

        List<String> exceptionNameList = exceptionInfo.get("exceptionNameList");
        List<String> valueList = exceptionInfo.get("valueList");

        ReportFactory reportFactory = new ReportFactory();
        reportFactory.genPrimJS(GlobalCons.REPORT_OUTPUT_FOLDER);
        reportFactory.genPrimCSS(GlobalCons.REPORT_OUTPUT_FOLDER);
        reportFactory.genMyCSS(GlobalCons.REPORT_OUTPUT_FOLDER);
        reportFactory.genMyJS(GlobalCons.REPORT_OUTPUT_FOLDER,
                classInfo.get(0).get("success_count"),
                classInfo.get(0).get("failure_count"),
                classInfo.get(0).get("skip_count"),exceptionNameList, valueList);
        reportFactory.genEchartJS(GlobalCons.REPORT_OUTPUT_FOLDER);
        reportFactory.genIndexHTML(GlobalCons.REPORT_OUTPUT_FOLDER,
                GlobalCons.PROJECT_NAME, classInfo.get(0), GlobalCons.EXECUTE_TIME,
                detailInfo.get("methodNameListTable"),detailInfo.get("classNameListTable"),detailInfo.get("exceptionNameListTable"));
        System.exit(0);
    }


    private static void generateSimpleHtml(){
        Map<String, List<String>> detailInfoMap = new SearchService().getDataForStaticHtml();

        //用于存储html字符串
        StringBuilder stringHtml = new StringBuilder();
        stringHtml.append("<!DOCTYPE html>\n <html>").
                append("<head>\n<meta charset=\"UTF-8\">\n").
//                append("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\" >\n").
        append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" >\n").
                append("<title>Justin Report</title>\n").
                append("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/aa.css\">\n").
                append("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/prism.css\">\n").
                append("<script src=\"https://code.jquery.com/jquery-3.5.1.min.js\"></script>\n").
                append("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/prism.css\">\n").
                append("<script src=\"js/prism.js\"></script>\n").
                append("</head>").
                append("<body>\n").
                append("<div class=\"topDiv\">\n").
                append("<div class=\"centerDiv\"><p>Test Result Report</p></div>\n").
                append("<div class=\"logoDiv\" id = \"logoDiv\">\n").append("<img class=\"logo\" ></div>\n").
                append("<div class=\"sumDiv\"><p>(Exception Num:").append(detailInfoMap.keySet().size()).append(")</p></div></div>");
        int count = 1;
        for (String key : detailInfoMap.keySet()) {
            stringHtml.append("<div class=\"infoDiv\">\n").
                    append("<p class=\"miniID\">NO:  #").append(count).append("</p>\n").
                    append("<div class=\"line\"></div>\n").
                    append("<p class=\"miniID\">Exception Type:  ").append(key.split("##")[3]).append("</p>\n ").
                    append("<p class=\"miniID\">Method Name:  ").append(key.split("##")[2]).append(":      ").append(key.split("##")[1]).append("</p>\n");
            List<String> strList = detailInfoMap.get(key);
            StringBuilder errorTestMethodName = new StringBuilder();
            for (int i = 0; i < strList.size(); i = i+3) {
                errorTestMethodName.append(strList.get(i)).append(", ");
            }
            stringHtml.append("<p class=\"miniID\">Test Method Name:  ").append(errorTestMethodName.substring(0, errorTestMethodName.length() - 2)).append("</p>\n").
                    append("<pre> <code class=\"language-java\"> <xmp> \n").
                    append(strList.get(1)).append("stackDetail:\n").append(strList.get(2)).
                    append("</xmp></code></pre></div>\n");
            count++;
        }

        stringHtml.append("</body>\n</html>");
        try{
            File reportFile = new File(System.getProperty("user.dir") + "\\report");
            if (!reportFile.exists()) {
                reportFile.mkdir();
            }

            File cssFile = new File(System.getProperty("user.dir") + "\\report\\css");
            cssFile.mkdir();

            File aaCSS = new File(System.getProperty("user.dir") + "\\report\\css\\aa.css");
            aaCSS.createNewFile();
            PrintStream printStreamCSS = new PrintStream(aaCSS);
            printStreamCSS.println("@charset \"UTF-8\";\n" +
                    "\n" +
                    "html,body {\n" +
                    "    Font-family: Helvetica, Tahoma, Arial, STXihei, “华文细黑”, “Microsoft YaHei”, “微软雅黑”, sans-serif;\n" +
                    "    height: 100%;\n" +
                    "    background: #f6f7fd;\n" +
                    "}\n" +
                    "\n" +
                    ".topDiv {\n" +
                    "    height: 70px;\n" +
                    "    position: relative;\n" +
                    "}\n" +
                    "\n" +
                    ".centerDiv{\n" +
                    "    left: 40%;\n" +
                    "    position: absolute;\n" +
                    "    font-size: 30px;\n" +
                    "    font-weight:bold;\n" +
                    "    color: rgba(24,50,255,0.55);\n" +
                    "}\n" +
                    ".sumDiv{\n" +
                    "    left: 43%;\n" +
                    "    position: relative;\n" +
                    "    height: 20px;\n" +
                    "    top: -40px;\n" +
                    "    font-size: 15px;\n" +
                    "    font-weight:bold;\n" +
                    "    color: rgba(24,50,255,0.55);\n" +
                    "}\n" +
                    "\n" +
                    "\n" +
                    ".logoDiv{\n" +
                    "    height: 100px;\n" +
                    "    position: relative;\n" +
                    "}\n" +
                    "\n" +
                    ".logo{\n" +
                    "    /*margin-top: 10px;*/\n" +
                    "    margin-left: 5px;\n" +
                    "    height: 60%;\n" +
                    "}\n" +
                    "\n" +
                    "\n" +
                    ".infoDiv{\n" +
                    "    position: relative;\n" +
                    "    top: 20px;\n" +
                    "    background-color: rgba(245,242,240,0.55);\n" +
                    "}\n" +
                    "\n" +
                    ".miniID{\n" +
                    "    position: relative;\n" +
                    "    margin-left: 20px;\n" +
                    "    font-size: 16px;\n" +
                    "    font-weight:bold;\n" +
                    "}\n" +
                    ".line {\n" +
                    "    margin-top: 10px;\n" +
                    "    height: 2px;\n" +
                    "    border-top: solid #ACC0D8;\n" +
                    "}");
            printStreamCSS.close();

            File prismCSS = new File(System.getProperty("user.dir") + "\\report\\css\\prism.css");
            prismCSS.createNewFile();
            PrintStream printStreamCSS2 = new PrintStream(prismCSS);
            printStreamCSS2.println("/* PrismJS 1.28.0\n" +
                    "https://prismjs.com/download.html#themes=prism&languages=markup+css+clike+javascript+css-extras+java&plugins=line-highlight+line-numbers+inline-color */\n" +
                    "code[class*=language-],pre[class*=language-]{color:#000;background:0 0;text-shadow:0 1px #fff;font-family:Consolas,Monaco,'Andale Mono','Ubuntu Mono',monospace;font-size:1em;text-align:left;white-space:pre;word-spacing:normal;word-break:normal;word-wrap:normal;line-height:1.5;-moz-tab-size:4;-o-tab-size:4;tab-size:4;-webkit-hyphens:none;-moz-hyphens:none;-ms-hyphens:none;hyphens:none}code[class*=language-] ::-moz-selection,code[class*=language-]::-moz-selection,pre[class*=language-] ::-moz-selection,pre[class*=language-]::-moz-selection{text-shadow:none;background:#b3d4fc}code[class*=language-] ::selection,code[class*=language-]::selection,pre[class*=language-] ::selection,pre[class*=language-]::selection{text-shadow:none;background:#b3d4fc}@media print{code[class*=language-],pre[class*=language-]{text-shadow:none}}pre[class*=language-]{padding:1em;margin:.5em 0;overflow:auto}:not(pre)>code[class*=language-],pre[class*=language-]{background:#f5f2f0}:not(pre)>code[class*=language-]{padding:.1em;border-radius:.3em;white-space:normal}.token.cdata,.token.comment,.token.doctype,.token.prolog{color:#708090}.token.punctuation{color:#999}.token.namespace{opacity:.7}.token.boolean,.token.constant,.token.deleted,.token.number,.token.property,.token.symbol,.token.tag{color:#905}.token.attr-name,.token.builtin,.token.char,.token.inserted,.token.selector,.token.string{color:#690}.language-css .token.string,.style .token.string,.token.entity,.token.operator,.token.url{color:#9a6e3a;background:hsla(0,0%,100%,.5)}.token.atrule,.token.attr-value,.token.keyword{color:#07a}.token.class-name,.token.function{color:#dd4a68}.token.important,.token.regex,.token.variable{color:#e90}.token.bold,.token.important{font-weight:700}.token.italic{font-style:italic}.token.entity{cursor:help}\n" +
                    "pre[data-line]{position:relative;padding:1em 0 1em 3em}.line-highlight{position:absolute;left:0;right:0;padding:inherit 0;margin-top:1em;background:hsla(24,20%,50%,.08);background:linear-gradient(to right,hsla(24,20%,50%,.1) 70%,hsla(24,20%,50%,0));pointer-events:none;line-height:inherit;white-space:pre}@media print{.line-highlight{-webkit-print-color-adjust:exact;color-adjust:exact}}.line-highlight:before,.line-highlight[data-end]:after{content:attr(data-start);position:absolute;top:.4em;left:.6em;min-width:1em;padding:0 .5em;background-color:hsla(24,20%,50%,.4);color:#f4f1ef;font:bold 65%/1.5 sans-serif;text-align:center;vertical-align:.3em;border-radius:999px;text-shadow:none;box-shadow:0 1px #fff}.line-highlight[data-end]:after{content:attr(data-end);top:auto;bottom:.4em}.line-numbers .line-highlight:after,.line-numbers .line-highlight:before{content:none}pre[id].linkable-line-numbers span.line-numbers-rows{pointer-events:all}pre[id].linkable-line-numbers span.line-numbers-rows>span:before{cursor:pointer}pre[id].linkable-line-numbers span.line-numbers-rows>span:hover:before{background-color:rgba(128,128,128,.2)}\n" +
                    "pre[class*=language-].line-numbers{position:relative;padding-left:3.8em;counter-reset:linenumber}pre[class*=language-].line-numbers>code{position:relative;white-space:inherit}.line-numbers .line-numbers-rows{position:absolute;pointer-events:none;top:0;font-size:100%;left:-3.8em;width:3em;letter-spacing:-1px;border-right:1px solid #999;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none}.line-numbers-rows>span{display:block;counter-increment:linenumber}.line-numbers-rows>span:before{content:counter(linenumber);color:#999;display:block;padding-right:.8em;text-align:right}\n" +
                    "span.inline-color-wrapper{background:url(data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyIDIiPjxwYXRoIGZpbGw9ImdyYXkiIGQ9Ik0wIDBoMnYySDB6Ii8+PHBhdGggZmlsbD0id2hpdGUiIGQ9Ik0wIDBoMXYxSDB6TTEgMWgxdjFIMXoiLz48L3N2Zz4=);background-position:center;background-size:110%;display:inline-block;height:1.333ch;width:1.333ch;margin:0 .333ch;box-sizing:border-box;border:1px solid #fff;outline:1px solid rgba(0,0,0,.5);overflow:hidden}span.inline-color{display:block;height:120%;width:120%}\n");
            printStreamCSS2.close();

//            File imagesFile = new File(System.getProperty("user.dir") + "\\report\\images");
//            imagesFile.mkdir();


            File jsFile = new File(System.getProperty("user.dir") + "\\report\\js");
            jsFile.mkdir();

            File prismJS = new File(System.getProperty("user.dir") + "\\report\\js\\prism.js");
            prismJS.createNewFile();
            PrintStream printStreamJS = new PrintStream(prismJS);
            printStreamJS.println("/* PrismJS 1.28.0\n" +
                    "https://prismjs.com/download.html#themes=prism&languages=markup+css+clike+javascript+css-extras+java&plugins=line-highlight+line-numbers+inline-color */\n" +
                    "var _self=\"undefined\"!=typeof window?window:\"undefined\"!=typeof WorkerGlobalScope&&self instanceof WorkerGlobalScope?self:{},Prism=function(e){var n=/(?:^|\\s)lang(?:uage)?-([\\w-]+)(?=\\s|$)/i,t=0,r={},a={manual:e.Prism&&e.Prism.manual,disableWorkerMessageHandler:e.Prism&&e.Prism.disableWorkerMessageHandler,util:{encode:function e(n){return n instanceof i?new i(n.type,e(n.content),n.alias):Array.isArray(n)?n.map(e):n.replace(/&/g,\"&amp;\").replace(/</g,\"&lt;\").replace(/\\u00a0/g,\" \")},type:function(e){return Object.prototype.toString.call(e).slice(8,-1)},objId:function(e){return e.__id||Object.defineProperty(e,\"__id\",{value:++t}),e.__id},clone:function e(n,t){var r,i;switch(t=t||{},a.util.type(n)){case\"Object\":if(i=a.util.objId(n),t[i])return t[i];for(var l in r={},t[i]=r,n)n.hasOwnProperty(l)&&(r[l]=e(n[l],t));return r;case\"Array\":return i=a.util.objId(n),t[i]?t[i]:(r=[],t[i]=r,n.forEach((function(n,a){r[a]=e(n,t)})),r);default:return n}},getLanguage:function(e){for(;e;){var t=n.exec(e.className);if(t)return t[1].toLowerCase();e=e.parentElement}return\"none\"},setLanguage:function(e,t){e.className=e.className.replace(RegExp(n,\"gi\"),\"\"),e.classList.add(\"language-\"+t)},currentScript:function(){if(\"undefined\"==typeof document)return null;if(\"currentScript\"in document)return document.currentScript;try{throw new Error}catch(r){var e=(/at [^(\\r\\n]*\\((.*):[^:]+:[^:]+\\)$/i.exec(r.stack)||[])[1];if(e){var n=document.getElementsByTagName(\"script\");for(var t in n)if(n[t].src==e)return n[t]}return null}},isActive:function(e,n,t){for(var r=\"no-\"+n;e;){var a=e.classList;if(a.contains(n))return!0;if(a.contains(r))return!1;e=e.parentElement}return!!t}},languages:{plain:r,plaintext:r,text:r,txt:r,extend:function(e,n){var t=a.util.clone(a.languages[e]);for(var r in n)t[r]=n[r];return t},insertBefore:function(e,n,t,r){var i=(r=r||a.languages)[e],l={};for(var o in i)if(i.hasOwnProperty(o)){if(o==n)for(var s in t)t.hasOwnProperty(s)&&(l[s]=t[s]);t.hasOwnProperty(o)||(l[o]=i[o])}var u=r[e];return r[e]=l,a.languages.DFS(a.languages,(function(n,t){t===u&&n!=e&&(this[n]=l)})),l},DFS:function e(n,t,r,i){i=i||{};var l=a.util.objId;for(var o in n)if(n.hasOwnProperty(o)){t.call(n,o,n[o],r||o);var s=n[o],u=a.util.type(s);\"Object\"!==u||i[l(s)]?\"Array\"!==u||i[l(s)]||(i[l(s)]=!0,e(s,t,o,i)):(i[l(s)]=!0,e(s,t,null,i))}}},plugins:{},highlightAll:function(e,n){a.highlightAllUnder(document,e,n)},highlightAllUnder:function(e,n,t){var r={callback:t,container:e,selector:'code[class*=\"language-\"], [class*=\"language-\"] code, code[class*=\"lang-\"], [class*=\"lang-\"] code'};a.hooks.run(\"before-highlightall\",r),r.elements=Array.prototype.slice.apply(r.container.querySelectorAll(r.selector)),a.hooks.run(\"before-all-elements-highlight\",r);for(var i,l=0;i=r.elements[l++];)a.highlightElement(i,!0===n,r.callback)},highlightElement:function(n,t,r){var i=a.util.getLanguage(n),l=a.languages[i];a.util.setLanguage(n,i);var o=n.parentElement;o&&\"pre\"===o.nodeName.toLowerCase()&&a.util.setLanguage(o,i);var s={element:n,language:i,grammar:l,code:n.textContent};function u(e){s.highlightedCode=e,a.hooks.run(\"before-insert\",s),s.element.innerHTML=s.highlightedCode,a.hooks.run(\"after-highlight\",s),a.hooks.run(\"complete\",s),r&&r.call(s.element)}if(a.hooks.run(\"before-sanity-check\",s),(o=s.element.parentElement)&&\"pre\"===o.nodeName.toLowerCase()&&!o.hasAttribute(\"tabindex\")&&o.setAttribute(\"tabindex\",\"0\"),!s.code)return a.hooks.run(\"complete\",s),void(r&&r.call(s.element));if(a.hooks.run(\"before-highlight\",s),s.grammar)if(t&&e.Worker){var c=new Worker(a.filename);c.onmessage=function(e){u(e.data)},c.postMessage(JSON.stringify({language:s.language,code:s.code,immediateClose:!0}))}else u(a.highlight(s.code,s.grammar,s.language));else u(a.util.encode(s.code))},highlight:function(e,n,t){var r={code:e,grammar:n,language:t};if(a.hooks.run(\"before-tokenize\",r),!r.grammar)throw new Error('The language \"'+r.language+'\" has no grammar.');return r.tokens=a.tokenize(r.code,r.grammar),a.hooks.run(\"after-tokenize\",r),i.stringify(a.util.encode(r.tokens),r.language)},tokenize:function(e,n){var t=n.rest;if(t){for(var r in t)n[r]=t[r];delete n.rest}var a=new s;return u(a,a.head,e),o(e,a,n,a.head,0),function(e){for(var n=[],t=e.head.next;t!==e.tail;)n.push(t.value),t=t.next;return n}(a)},hooks:{all:{},add:function(e,n){var t=a.hooks.all;t[e]=t[e]||[],t[e].push(n)},run:function(e,n){var t=a.hooks.all[e];if(t&&t.length)for(var r,i=0;r=t[i++];)r(n)}},Token:i};function i(e,n,t,r){this.type=e,this.content=n,this.alias=t,this.length=0|(r||\"\").length}function l(e,n,t,r){e.lastIndex=n;var a=e.exec(t);if(a&&r&&a[1]){var i=a[1].length;a.index+=i,a[0]=a[0].slice(i)}return a}function o(e,n,t,r,s,g){for(var f in t)if(t.hasOwnProperty(f)&&t[f]){var h=t[f];h=Array.isArray(h)?h:[h];for(var d=0;d<h.length;++d){if(g&&g.cause==f+\",\"+d)return;var v=h[d],p=v.inside,m=!!v.lookbehind,y=!!v.greedy,k=v.alias;if(y&&!v.pattern.global){var x=v.pattern.toString().match(/[imsuy]*$/)[0];v.pattern=RegExp(v.pattern.source,x+\"g\")}for(var b=v.pattern||v,w=r.next,A=s;w!==n.tail&&!(g&&A>=g.reach);A+=w.value.length,w=w.next){var E=w.value;if(n.length>e.length)return;if(!(E instanceof i)){var P,L=1;if(y){if(!(P=l(b,A,e,m))||P.index>=e.length)break;var S=P.index,O=P.index+P[0].length,j=A;for(j+=w.value.length;S>=j;)j+=(w=w.next).value.length;if(A=j-=w.value.length,w.value instanceof i)continue;for(var C=w;C!==n.tail&&(j<O||\"string\"==typeof C.value);C=C.next)L++,j+=C.value.length;L--,E=e.slice(A,j),P.index-=A}else if(!(P=l(b,0,E,m)))continue;S=P.index;var N=P[0],_=E.slice(0,S),M=E.slice(S+N.length),W=A+E.length;g&&W>g.reach&&(g.reach=W);var z=w.prev;if(_&&(z=u(n,z,_),A+=_.length),c(n,z,L),w=u(n,z,new i(f,p?a.tokenize(N,p):N,k,N)),M&&u(n,w,M),L>1){var I={cause:f+\",\"+d,reach:W};o(e,n,t,w.prev,A,I),g&&I.reach>g.reach&&(g.reach=I.reach)}}}}}}function s(){var e={value:null,prev:null,next:null},n={value:null,prev:e,next:null};e.next=n,this.head=e,this.tail=n,this.length=0}function u(e,n,t){var r=n.next,a={value:t,prev:n,next:r};return n.next=a,r.prev=a,e.length++,a}function c(e,n,t){for(var r=n.next,a=0;a<t&&r!==e.tail;a++)r=r.next;n.next=r,r.prev=n,e.length-=a}if(e.Prism=a,i.stringify=function e(n,t){if(\"string\"==typeof n)return n;if(Array.isArray(n)){var r=\"\";return n.forEach((function(n){r+=e(n,t)})),r}var i={type:n.type,content:e(n.content,t),tag:\"span\",classes:[\"token\",n.type],attributes:{},language:t},l=n.alias;l&&(Array.isArray(l)?Array.prototype.push.apply(i.classes,l):i.classes.push(l)),a.hooks.run(\"wrap\",i);var o=\"\";for(var s in i.attributes)o+=\" \"+s+'=\"'+(i.attributes[s]||\"\").replace(/\"/g,\"&quot;\")+'\"';return\"<\"+i.tag+' class=\"'+i.classes.join(\" \")+'\"'+o+\">\"+i.content+\"</\"+i.tag+\">\"},!e.document)return e.addEventListener?(a.disableWorkerMessageHandler||e.addEventListener(\"message\",(function(n){var t=JSON.parse(n.data),r=t.language,i=t.code,l=t.immediateClose;e.postMessage(a.highlight(i,a.languages[r],r)),l&&e.close()}),!1),a):a;var g=a.util.currentScript();function f(){a.manual||a.highlightAll()}if(g&&(a.filename=g.src,g.hasAttribute(\"data-manual\")&&(a.manual=!0)),!a.manual){var h=document.readyState;\"loading\"===h||\"interactive\"===h&&g&&g.defer?document.addEventListener(\"DOMContentLoaded\",f):window.requestAnimationFrame?window.requestAnimationFrame(f):window.setTimeout(f,16)}return a}(_self);\"undefined\"!=typeof module&&module.exports&&(module.exports=Prism),\"undefined\"!=typeof global&&(global.Prism=Prism);\n" +
                    "Prism.languages.markup={comment:{pattern:/<!--(?:(?!<!--)[\\s\\S])*?-->/,greedy:!0},prolog:{pattern:/<\\?[\\s\\S]+?\\?>/,greedy:!0},doctype:{pattern:/<!DOCTYPE(?:[^>\"'[\\]]|\"[^\"]*\"|'[^']*')+(?:\\[(?:[^<\"'\\]]|\"[^\"]*\"|'[^']*'|<(?!!--)|<!--(?:[^-]|-(?!->))*-->)*\\]\\s*)?>/i,greedy:!0,inside:{\"internal-subset\":{pattern:/(^[^\\[]*\\[)[\\s\\S]+(?=\\]>$)/,lookbehind:!0,greedy:!0,inside:null},string:{pattern:/\"[^\"]*\"|'[^']*'/,greedy:!0},punctuation:/^<!|>$|[[\\]]/,\"doctype-tag\":/^DOCTYPE/i,name:/[^\\s<>'\"]+/}},cdata:{pattern:/<!\\[CDATA\\[[\\s\\S]*?\\]\\]>/i,greedy:!0},tag:{pattern:/<\\/?(?!\\d)[^\\s>\\/=$<%]+(?:\\s(?:\\s*[^\\s>\\/=]+(?:\\s*=\\s*(?:\"[^\"]*\"|'[^']*'|[^\\s'\">=]+(?=[\\s>]))|(?=[\\s/>])))+)?\\s*\\/?>/,greedy:!0,inside:{tag:{pattern:/^<\\/?[^\\s>\\/]+/,inside:{punctuation:/^<\\/?/,namespace:/^[^\\s>\\/:]+:/}},\"special-attr\":[],\"attr-value\":{pattern:/=\\s*(?:\"[^\"]*\"|'[^']*'|[^\\s'\">=]+)/,inside:{punctuation:[{pattern:/^=/,alias:\"attr-equals\"},{pattern:/^(\\s*)[\"']|[\"']$/,lookbehind:!0}]}},punctuation:/\\/?>/,\"attr-name\":{pattern:/[^\\s>\\/]+/,inside:{namespace:/^[^\\s>\\/:]+:/}}}},entity:[{pattern:/&[\\da-z]{1,8};/i,alias:\"named-entity\"},/&#x?[\\da-f]{1,8};/i]},Prism.languages.markup.tag.inside[\"attr-value\"].inside.entity=Prism.languages.markup.entity,Prism.languages.markup.doctype.inside[\"internal-subset\"].inside=Prism.languages.markup,Prism.hooks.add(\"wrap\",(function(a){\"entity\"===a.type&&(a.attributes.title=a.content.replace(/&amp;/,\"&\"))})),Object.defineProperty(Prism.languages.markup.tag,\"addInlined\",{value:function(a,e){var s={};s[\"language-\"+e]={pattern:/(^<!\\[CDATA\\[)[\\s\\S]+?(?=\\]\\]>$)/i,lookbehind:!0,inside:Prism.languages[e]},s.cdata=/^<!\\[CDATA\\[|\\]\\]>$/i;var t={\"included-cdata\":{pattern:/<!\\[CDATA\\[[\\s\\S]*?\\]\\]>/i,inside:s}};t[\"language-\"+e]={pattern:/[\\s\\S]+/,inside:Prism.languages[e]};var n={};n[a]={pattern:RegExp(\"(<__[^>]*>)(?:<!\\\\[CDATA\\\\[(?:[^\\\\]]|\\\\](?!\\\\]>))*\\\\]\\\\]>|(?!<!\\\\[CDATA\\\\[)[^])*?(?=</__>)\".replace(/__/g,(function(){return a})),\"i\"),lookbehind:!0,greedy:!0,inside:t},Prism.languages.insertBefore(\"markup\",\"cdata\",n)}}),Object.defineProperty(Prism.languages.markup.tag,\"addAttribute\",{value:function(a,e){Prism.languages.markup.tag.inside[\"special-attr\"].push({pattern:RegExp(\"(^|[\\\"'\\\\s])(?:\"+a+\")\\\\s*=\\\\s*(?:\\\"[^\\\"]*\\\"|'[^']*'|[^\\\\s'\\\">=]+(?=[\\\\s>]))\",\"i\"),lookbehind:!0,inside:{\"attr-name\":/^[^\\s=]+/,\"attr-value\":{pattern:/=[\\s\\S]+/,inside:{value:{pattern:/(^=\\s*([\"']|(?![\"'])))\\S[\\s\\S]*(?=\\2$)/,lookbehind:!0,alias:[e,\"language-\"+e],inside:Prism.languages[e]},punctuation:[{pattern:/^=/,alias:\"attr-equals\"},/\"|'/]}}}})}}),Prism.languages.html=Prism.languages.markup,Prism.languages.mathml=Prism.languages.markup,Prism.languages.svg=Prism.languages.markup,Prism.languages.xml=Prism.languages.extend(\"markup\",{}),Prism.languages.ssml=Prism.languages.xml,Prism.languages.atom=Prism.languages.xml,Prism.languages.rss=Prism.languages.xml;\n" +
                    "!function(s){var e=/(?:\"(?:\\\\(?:\\r\\n|[\\s\\S])|[^\"\\\\\\r\\n])*\"|'(?:\\\\(?:\\r\\n|[\\s\\S])|[^'\\\\\\r\\n])*')/;s.languages.css={comment:/\\/\\*[\\s\\S]*?\\*\\//,atrule:{pattern:RegExp(\"@[\\\\w-](?:[^;{\\\\s\\\"']|\\\\s+(?!\\\\s)|\"+e.source+\")*?(?:;|(?=\\\\s*\\\\{))\"),inside:{rule:/^@[\\w-]+/,\"selector-function-argument\":{pattern:/(\\bselector\\s*\\(\\s*(?![\\s)]))(?:[^()\\s]|\\s+(?![\\s)])|\\((?:[^()]|\\([^()]*\\))*\\))+(?=\\s*\\))/,lookbehind:!0,alias:\"selector\"},keyword:{pattern:/(^|[^\\w-])(?:and|not|only|or)(?![\\w-])/,lookbehind:!0}}},url:{pattern:RegExp(\"\\\\burl\\\\((?:\"+e.source+\"|(?:[^\\\\\\\\\\r\\n()\\\"']|\\\\\\\\[^])*)\\\\)\",\"i\"),greedy:!0,inside:{function:/^url/i,punctuation:/^\\(|\\)$/,string:{pattern:RegExp(\"^\"+e.source+\"$\"),alias:\"url\"}}},selector:{pattern:RegExp(\"(^|[{}\\\\s])[^{}\\\\s](?:[^{};\\\"'\\\\s]|\\\\s+(?![\\\\s{])|\"+e.source+\")*(?=\\\\s*\\\\{)\"),lookbehind:!0},string:{pattern:e,greedy:!0},property:{pattern:/(^|[^-\\w\\xA0-\\uFFFF])(?!\\s)[-_a-z\\xA0-\\uFFFF](?:(?!\\s)[-\\w\\xA0-\\uFFFF])*(?=\\s*:)/i,lookbehind:!0},important:/!important\\b/i,function:{pattern:/(^|[^-a-z0-9])[-a-z0-9]+(?=\\()/i,lookbehind:!0},punctuation:/[(){};:,]/},s.languages.css.atrule.inside.rest=s.languages.css;var t=s.languages.markup;t&&(t.tag.addInlined(\"style\",\"css\"),t.tag.addAttribute(\"style\",\"css\"))}(Prism);\n" +
                    "Prism.languages.clike={comment:[{pattern:/(^|[^\\\\])\\/\\*[\\s\\S]*?(?:\\*\\/|$)/,lookbehind:!0,greedy:!0},{pattern:/(^|[^\\\\:])\\/\\/.*/,lookbehind:!0,greedy:!0}],string:{pattern:/([\"'])(?:\\\\(?:\\r\\n|[\\s\\S])|(?!\\1)[^\\\\\\r\\n])*\\1/,greedy:!0},\"class-name\":{pattern:/(\\b(?:class|extends|implements|instanceof|interface|new|trait)\\s+|\\bcatch\\s+\\()[\\w.\\\\]+/i,lookbehind:!0,inside:{punctuation:/[.\\\\]/}},keyword:/\\b(?:break|catch|continue|do|else|finally|for|function|if|in|instanceof|new|null|return|throw|try|while)\\b/,boolean:/\\b(?:false|true)\\b/,function:/\\b\\w+(?=\\()/,number:/\\b0x[\\da-f]+\\b|(?:\\b\\d+(?:\\.\\d*)?|\\B\\.\\d+)(?:e[+-]?\\d+)?/i,operator:/[<>]=?|[!=]=?=?|--?|\\+\\+?|&&?|\\|\\|?|[?*/~^%]/,punctuation:/[{}[\\];(),.:]/};\n" +
                    "Prism.languages.javascript=Prism.languages.extend(\"clike\",{\"class-name\":[Prism.languages.clike[\"class-name\"],{pattern:/(^|[^$\\w\\xA0-\\uFFFF])(?!\\s)[_$A-Z\\xA0-\\uFFFF](?:(?!\\s)[$\\w\\xA0-\\uFFFF])*(?=\\.(?:constructor|prototype))/,lookbehind:!0}],keyword:[{pattern:/((?:^|\\})\\s*)catch\\b/,lookbehind:!0},{pattern:/(^|[^.]|\\.\\.\\.\\s*)\\b(?:as|assert(?=\\s*\\{)|async(?=\\s*(?:function\\b|\\(|[$\\w\\xA0-\\uFFFF]|$))|await|break|case|class|const|continue|debugger|default|delete|do|else|enum|export|extends|finally(?=\\s*(?:\\{|$))|for|from(?=\\s*(?:['\"]|$))|function|(?:get|set)(?=\\s*(?:[#\\[$\\w\\xA0-\\uFFFF]|$))|if|implements|import|in|instanceof|interface|let|new|null|of|package|private|protected|public|return|static|super|switch|this|throw|try|typeof|undefined|var|void|while|with|yield)\\b/,lookbehind:!0}],function:/#?(?!\\s)[_$a-zA-Z\\xA0-\\uFFFF](?:(?!\\s)[$\\w\\xA0-\\uFFFF])*(?=\\s*(?:\\.\\s*(?:apply|bind|call)\\s*)?\\()/,number:{pattern:RegExp(\"(^|[^\\\\w$])(?:NaN|Infinity|0[bB][01]+(?:_[01]+)*n?|0[oO][0-7]+(?:_[0-7]+)*n?|0[xX][\\\\dA-Fa-f]+(?:_[\\\\dA-Fa-f]+)*n?|\\\\d+(?:_\\\\d+)*n|(?:\\\\d+(?:_\\\\d+)*(?:\\\\.(?:\\\\d+(?:_\\\\d+)*)?)?|\\\\.\\\\d+(?:_\\\\d+)*)(?:[Ee][+-]?\\\\d+(?:_\\\\d+)*)?)(?![\\\\w$])\"),lookbehind:!0},operator:/--|\\+\\+|\\*\\*=?|=>|&&=?|\\|\\|=?|[!=]==|<<=?|>>>?=?|[-+*/%&|^!=<>]=?|\\.{3}|\\?\\?=?|\\?\\.?|[~:]/}),Prism.languages.javascript[\"class-name\"][0].pattern=/(\\b(?:class|extends|implements|instanceof|interface|new)\\s+)[\\w.\\\\]+/,Prism.languages.insertBefore(\"javascript\",\"keyword\",{regex:{pattern:RegExp(\"((?:^|[^$\\\\w\\\\xA0-\\\\uFFFF.\\\"'\\\\])\\\\s]|\\\\b(?:return|yield))\\\\s*)/(?:(?:\\\\[(?:[^\\\\]\\\\\\\\\\r\\n]|\\\\\\\\.)*\\\\]|\\\\\\\\.|[^/\\\\\\\\\\\\[\\r\\n])+/[dgimyus]{0,7}|(?:\\\\[(?:[^[\\\\]\\\\\\\\\\r\\n]|\\\\\\\\.|\\\\[(?:[^[\\\\]\\\\\\\\\\r\\n]|\\\\\\\\.|\\\\[(?:[^[\\\\]\\\\\\\\\\r\\n]|\\\\\\\\.)*\\\\])*\\\\])*\\\\]|\\\\\\\\.|[^/\\\\\\\\\\\\[\\r\\n])+/[dgimyus]{0,7}v[dgimyus]{0,7})(?=(?:\\\\s|/\\\\*(?:[^*]|\\\\*(?!/))*\\\\*/)*(?:$|[\\r\\n,.;:})\\\\]]|//))\"),lookbehind:!0,greedy:!0,inside:{\"regex-source\":{pattern:/^(\\/)[\\s\\S]+(?=\\/[a-z]*$)/,lookbehind:!0,alias:\"language-regex\",inside:Prism.languages.regex},\"regex-delimiter\":/^\\/|\\/$/,\"regex-flags\":/^[a-z]+$/}},\"function-variable\":{pattern:/#?(?!\\s)[_$a-zA-Z\\xA0-\\uFFFF](?:(?!\\s)[$\\w\\xA0-\\uFFFF])*(?=\\s*[=:]\\s*(?:async\\s*)?(?:\\bfunction\\b|(?:\\((?:[^()]|\\([^()]*\\))*\\)|(?!\\s)[_$a-zA-Z\\xA0-\\uFFFF](?:(?!\\s)[$\\w\\xA0-\\uFFFF])*)\\s*=>))/,alias:\"function\"},parameter:[{pattern:/(function(?:\\s+(?!\\s)[_$a-zA-Z\\xA0-\\uFFFF](?:(?!\\s)[$\\w\\xA0-\\uFFFF])*)?\\s*\\(\\s*)(?!\\s)(?:[^()\\s]|\\s+(?![\\s)])|\\([^()]*\\))+(?=\\s*\\))/,lookbehind:!0,inside:Prism.languages.javascript},{pattern:/(^|[^$\\w\\xA0-\\uFFFF])(?!\\s)[_$a-z\\xA0-\\uFFFF](?:(?!\\s)[$\\w\\xA0-\\uFFFF])*(?=\\s*=>)/i,lookbehind:!0,inside:Prism.languages.javascript},{pattern:/(\\(\\s*)(?!\\s)(?:[^()\\s]|\\s+(?![\\s)])|\\([^()]*\\))+(?=\\s*\\)\\s*=>)/,lookbehind:!0,inside:Prism.languages.javascript},{pattern:/((?:\\b|\\s|^)(?!(?:as|async|await|break|case|catch|class|const|continue|debugger|default|delete|do|else|enum|export|extends|finally|for|from|function|get|if|implements|import|in|instanceof|interface|let|new|null|of|package|private|protected|public|return|set|static|super|switch|this|throw|try|typeof|undefined|var|void|while|with|yield)(?![$\\w\\xA0-\\uFFFF]))(?:(?!\\s)[_$a-zA-Z\\xA0-\\uFFFF](?:(?!\\s)[$\\w\\xA0-\\uFFFF])*\\s*)\\(\\s*|\\]\\s*\\(\\s*)(?!\\s)(?:[^()\\s]|\\s+(?![\\s)])|\\([^()]*\\))+(?=\\s*\\)\\s*\\{)/,lookbehind:!0,inside:Prism.languages.javascript}],constant:/\\b[A-Z](?:[A-Z_]|\\dx?)*\\b/}),Prism.languages.insertBefore(\"javascript\",\"string\",{hashbang:{pattern:/^#!.*/,greedy:!0,alias:\"comment\"},\"template-string\":{pattern:/`(?:\\\\[\\s\\S]|\\$\\{(?:[^{}]|\\{(?:[^{}]|\\{[^}]*\\})*\\})+\\}|(?!\\$\\{)[^\\\\`])*`/,greedy:!0,inside:{\"template-punctuation\":{pattern:/^`|`$/,alias:\"string\"},interpolation:{pattern:/((?:^|[^\\\\])(?:\\\\{2})*)\\$\\{(?:[^{}]|\\{(?:[^{}]|\\{[^}]*\\})*\\})+\\}/,lookbehind:!0,inside:{\"interpolation-punctuation\":{pattern:/^\\$\\{|\\}$/,alias:\"punctuation\"},rest:Prism.languages.javascript}},string:/[\\s\\S]+/}},\"string-property\":{pattern:/((?:^|[,{])[ \\t]*)([\"'])(?:\\\\(?:\\r\\n|[\\s\\S])|(?!\\2)[^\\\\\\r\\n])*\\2(?=\\s*:)/m,lookbehind:!0,greedy:!0,alias:\"property\"}}),Prism.languages.insertBefore(\"javascript\",\"operator\",{\"literal-property\":{pattern:/((?:^|[,{])[ \\t]*)(?!\\s)[_$a-zA-Z\\xA0-\\uFFFF](?:(?!\\s)[$\\w\\xA0-\\uFFFF])*(?=\\s*:)/m,lookbehind:!0,alias:\"property\"}}),Prism.languages.markup&&(Prism.languages.markup.tag.addInlined(\"script\",\"javascript\"),Prism.languages.markup.tag.addAttribute(\"on(?:abort|blur|change|click|composition(?:end|start|update)|dblclick|error|focus(?:in|out)?|key(?:down|up)|load|mouse(?:down|enter|leave|move|out|over|up)|reset|resize|scroll|select|slotchange|submit|unload|wheel)\",\"javascript\")),Prism.languages.js=Prism.languages.javascript;\n" +
                    "!function(e){var a,n=/(\"|')(?:\\\\(?:\\r\\n|[\\s\\S])|(?!\\1)[^\\\\\\r\\n])*\\1/;e.languages.css.selector={pattern:e.languages.css.selector.pattern,lookbehind:!0,inside:a={\"pseudo-element\":/:(?:after|before|first-letter|first-line|selection)|::[-\\w]+/,\"pseudo-class\":/:[-\\w]+/,class:/\\.[-\\w]+/,id:/#[-\\w]+/,attribute:{pattern:RegExp(\"\\\\[(?:[^[\\\\]\\\"']|\"+n.source+\")*\\\\]\"),greedy:!0,inside:{punctuation:/^\\[|\\]$/,\"case-sensitivity\":{pattern:/(\\s)[si]$/i,lookbehind:!0,alias:\"keyword\"},namespace:{pattern:/^(\\s*)(?:(?!\\s)[-*\\w\\xA0-\\uFFFF])*\\|(?!=)/,lookbehind:!0,inside:{punctuation:/\\|$/}},\"attr-name\":{pattern:/^(\\s*)(?:(?!\\s)[-\\w\\xA0-\\uFFFF])+/,lookbehind:!0},\"attr-value\":[n,{pattern:/(=\\s*)(?:(?!\\s)[-\\w\\xA0-\\uFFFF])+(?=\\s*$)/,lookbehind:!0}],operator:/[|~*^$]?=/}},\"n-th\":[{pattern:/(\\(\\s*)[+-]?\\d*[\\dn](?:\\s*[+-]\\s*\\d+)?(?=\\s*\\))/,lookbehind:!0,inside:{number:/[\\dn]+/,operator:/[+-]/}},{pattern:/(\\(\\s*)(?:even|odd)(?=\\s*\\))/i,lookbehind:!0}],combinator:/>|\\+|~|\\|\\|/,punctuation:/[(),]/}},e.languages.css.atrule.inside[\"selector-function-argument\"].inside=a,e.languages.insertBefore(\"css\",\"property\",{variable:{pattern:/(^|[^-\\w\\xA0-\\uFFFF])--(?!\\s)[-_a-z\\xA0-\\uFFFF](?:(?!\\s)[-\\w\\xA0-\\uFFFF])*/i,lookbehind:!0}});var r={pattern:/(\\b\\d+)(?:%|[a-z]+(?![\\w-]))/,lookbehind:!0},i={pattern:/(^|[^\\w.-])-?(?:\\d+(?:\\.\\d+)?|\\.\\d+)/,lookbehind:!0};e.languages.insertBefore(\"css\",\"function\",{operator:{pattern:/(\\s)[+\\-*\\/](?=\\s)/,lookbehind:!0},hexcode:{pattern:/\\B#[\\da-f]{3,8}\\b/i,alias:\"color\"},color:[{pattern:/(^|[^\\w-])(?:AliceBlue|AntiqueWhite|Aqua|Aquamarine|Azure|Beige|Bisque|Black|BlanchedAlmond|Blue|BlueViolet|Brown|BurlyWood|CadetBlue|Chartreuse|Chocolate|Coral|CornflowerBlue|Cornsilk|Crimson|Cyan|DarkBlue|DarkCyan|DarkGoldenRod|DarkGr[ae]y|DarkGreen|DarkKhaki|DarkMagenta|DarkOliveGreen|DarkOrange|DarkOrchid|DarkRed|DarkSalmon|DarkSeaGreen|DarkSlateBlue|DarkSlateGr[ae]y|DarkTurquoise|DarkViolet|DeepPink|DeepSkyBlue|DimGr[ae]y|DodgerBlue|FireBrick|FloralWhite|ForestGreen|Fuchsia|Gainsboro|GhostWhite|Gold|GoldenRod|Gr[ae]y|Green|GreenYellow|HoneyDew|HotPink|IndianRed|Indigo|Ivory|Khaki|Lavender|LavenderBlush|LawnGreen|LemonChiffon|LightBlue|LightCoral|LightCyan|LightGoldenRodYellow|LightGr[ae]y|LightGreen|LightPink|LightSalmon|LightSeaGreen|LightSkyBlue|LightSlateGr[ae]y|LightSteelBlue|LightYellow|Lime|LimeGreen|Linen|Magenta|Maroon|MediumAquaMarine|MediumBlue|MediumOrchid|MediumPurple|MediumSeaGreen|MediumSlateBlue|MediumSpringGreen|MediumTurquoise|MediumVioletRed|MidnightBlue|MintCream|MistyRose|Moccasin|NavajoWhite|Navy|OldLace|Olive|OliveDrab|Orange|OrangeRed|Orchid|PaleGoldenRod|PaleGreen|PaleTurquoise|PaleVioletRed|PapayaWhip|PeachPuff|Peru|Pink|Plum|PowderBlue|Purple|RebeccaPurple|Red|RosyBrown|RoyalBlue|SaddleBrown|Salmon|SandyBrown|SeaGreen|SeaShell|Sienna|Silver|SkyBlue|SlateBlue|SlateGr[ae]y|Snow|SpringGreen|SteelBlue|Tan|Teal|Thistle|Tomato|Transparent|Turquoise|Violet|Wheat|White|WhiteSmoke|Yellow|YellowGreen)(?![\\w-])/i,lookbehind:!0},{pattern:/\\b(?:hsl|rgb)\\(\\s*\\d{1,3}\\s*,\\s*\\d{1,3}%?\\s*,\\s*\\d{1,3}%?\\s*\\)\\B|\\b(?:hsl|rgb)a\\(\\s*\\d{1,3}\\s*,\\s*\\d{1,3}%?\\s*,\\s*\\d{1,3}%?\\s*,\\s*(?:0|0?\\.\\d+|1)\\s*\\)\\B/i,inside:{unit:r,number:i,function:/[\\w-]+(?=\\()/,punctuation:/[(),]/}}],entity:/\\\\[\\da-f]{1,8}/i,unit:r,number:i})}(Prism);\n" +
                    "!function(e){var n=/\\b(?:abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|exports|extends|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|module|native|new|non-sealed|null|open|opens|package|permits|private|protected|provides|public|record(?!\\s*[(){}[\\]<>=%~.:,;?+\\-*/&|^])|requires|return|sealed|short|static|strictfp|super|switch|synchronized|this|throw|throws|to|transient|transitive|try|uses|var|void|volatile|while|with|yield)\\b/,t=\"(?:[a-z]\\\\w*\\\\s*\\\\.\\\\s*)*(?:[A-Z]\\\\w*\\\\s*\\\\.\\\\s*)*\",s={pattern:RegExp(\"(^|[^\\\\w.])\"+t+\"[A-Z](?:[\\\\d_A-Z]*[a-z]\\\\w*)?\\\\b\"),lookbehind:!0,inside:{namespace:{pattern:/^[a-z]\\w*(?:\\s*\\.\\s*[a-z]\\w*)*(?:\\s*\\.)?/,inside:{punctuation:/\\./}},punctuation:/\\./}};e.languages.java=e.languages.extend(\"clike\",{string:{pattern:/(^|[^\\\\])\"(?:\\\\.|[^\"\\\\\\r\\n])*\"/,lookbehind:!0,greedy:!0},\"class-name\":[s,{pattern:RegExp(\"(^|[^\\\\w.])\"+t+\"[A-Z]\\\\w*(?=\\\\s+\\\\w+\\\\s*[;,=()]|\\\\s*(?:\\\\[[\\\\s,]*\\\\]\\\\s*)?::\\\\s*new\\\\b)\"),lookbehind:!0,inside:s.inside},{pattern:RegExp(\"(\\\\b(?:class|enum|extends|implements|instanceof|interface|new|record|throws)\\\\s+)\"+t+\"[A-Z]\\\\w*\\\\b\"),lookbehind:!0,inside:s.inside}],keyword:n,function:[e.languages.clike.function,{pattern:/(::\\s*)[a-z_]\\w*/,lookbehind:!0}],number:/\\b0b[01][01_]*L?\\b|\\b0x(?:\\.[\\da-f_p+-]+|[\\da-f_]+(?:\\.[\\da-f_p+-]+)?)\\b|(?:\\b\\d[\\d_]*(?:\\.[\\d_]*)?|\\B\\.\\d[\\d_]*)(?:e[+-]?\\d[\\d_]*)?[dfl]?/i,operator:{pattern:/(^|[^.])(?:<<=?|>>>?=?|->|--|\\+\\+|&&|\\|\\||::|[?:~]|[-+*/%&|^!=<>]=?)/m,lookbehind:!0}}),e.languages.insertBefore(\"java\",\"string\",{\"triple-quoted-string\":{pattern:/\"\"\"[ \\t]*[\\r\\n](?:(?:\"|\"\")?(?:\\\\.|[^\"\\\\]))*\"\"\"/,greedy:!0,alias:\"string\"},char:{pattern:/'(?:\\\\.|[^'\\\\\\r\\n]){1,6}'/,greedy:!0}}),e.languages.insertBefore(\"java\",\"class-name\",{annotation:{pattern:/(^|[^.])@\\w+(?:\\s*\\.\\s*\\w+)*/,lookbehind:!0,alias:\"punctuation\"},generics:{pattern:/<(?:[\\w\\s,.?]|&(?!&)|<(?:[\\w\\s,.?]|&(?!&)|<(?:[\\w\\s,.?]|&(?!&)|<(?:[\\w\\s,.?]|&(?!&))*>)*>)*>)*>/,inside:{\"class-name\":s,keyword:n,punctuation:/[<>(),.:]/,operator:/[?&|]/}},import:[{pattern:RegExp(\"(\\\\bimport\\\\s+)\"+t+\"(?:[A-Z]\\\\w*|\\\\*)(?=\\\\s*;)\"),lookbehind:!0,inside:{namespace:s.inside.namespace,punctuation:/\\./,operator:/\\*/,\"class-name\":/\\w+/}},{pattern:RegExp(\"(\\\\bimport\\\\s+static\\\\s+)\"+t+\"(?:\\\\w+|\\\\*)(?=\\\\s*;)\"),lookbehind:!0,alias:\"static\",inside:{namespace:s.inside.namespace,static:/\\b\\w+$/,punctuation:/\\./,operator:/\\*/,\"class-name\":/\\w+/}}],namespace:{pattern:RegExp(\"(\\\\b(?:exports|import(?:\\\\s+static)?|module|open|opens|package|provides|requires|to|transitive|uses|with)\\\\s+)(?!<keyword>)[a-z]\\\\w*(?:\\\\.[a-z]\\\\w*)*\\\\.?\".replace(/<keyword>/g,(function(){return n.source}))),lookbehind:!0,inside:{punctuation:/\\./}}})}(Prism);\n" +
                    "!function(){if(\"undefined\"!=typeof Prism&&\"undefined\"!=typeof document&&document.querySelector){var e,t=\"line-numbers\",i=\"linkable-line-numbers\",n=/\\n(?!$)/g,r=!0;Prism.plugins.lineHighlight={highlightLines:function(o,u,c){var h=(u=\"string\"==typeof u?u:o.getAttribute(\"data-line\")||\"\").replace(/\\s+/g,\"\").split(\",\").filter(Boolean),d=+o.getAttribute(\"data-line-offset\")||0,f=(function(){if(void 0===e){var t=document.createElement(\"div\");t.style.fontSize=\"13px\",t.style.lineHeight=\"1.5\",t.style.padding=\"0\",t.style.border=\"0\",t.innerHTML=\"&nbsp;<br />&nbsp;\",document.body.appendChild(t),e=38===t.offsetHeight,document.body.removeChild(t)}return e}()?parseInt:parseFloat)(getComputedStyle(o).lineHeight),p=Prism.util.isActive(o,t),g=o.querySelector(\"code\"),m=p?o:g||o,v=[],y=g.textContent.match(n),b=y?y.length+1:1,A=g&&m!=g?function(e,t){var i=getComputedStyle(e),n=getComputedStyle(t);function r(e){return+e.substr(0,e.length-2)}return t.offsetTop+r(n.borderTopWidth)+r(n.paddingTop)-r(i.paddingTop)}(o,g):0;h.forEach((function(e){var t=e.split(\"-\"),i=+t[0],n=+t[1]||i;if(!((n=Math.min(b,n))<i)){var r=o.querySelector('.line-highlight[data-range=\"'+e+'\"]')||document.createElement(\"div\");if(v.push((function(){r.setAttribute(\"aria-hidden\",\"true\"),r.setAttribute(\"data-range\",e),r.className=(c||\"\")+\" line-highlight\"})),p&&Prism.plugins.lineNumbers){var s=Prism.plugins.lineNumbers.getLine(o,i),l=Prism.plugins.lineNumbers.getLine(o,n);if(s){var a=s.offsetTop+A+\"px\";v.push((function(){r.style.top=a}))}if(l){var u=l.offsetTop-s.offsetTop+l.offsetHeight+\"px\";v.push((function(){r.style.height=u}))}}else v.push((function(){r.setAttribute(\"data-start\",String(i)),n>i&&r.setAttribute(\"data-end\",String(n)),r.style.top=(i-d-1)*f+A+\"px\",r.textContent=new Array(n-i+2).join(\" \\n\")}));v.push((function(){r.style.width=o.scrollWidth+\"px\"})),v.push((function(){m.appendChild(r)}))}}));var P=o.id;if(p&&Prism.util.isActive(o,i)&&P){l(o,i)||v.push((function(){o.classList.add(i)}));var E=parseInt(o.getAttribute(\"data-start\")||\"1\");s(\".line-numbers-rows > span\",o).forEach((function(e,t){var i=t+E;e.onclick=function(){var e=P+\".\"+i;r=!1,location.hash=e,setTimeout((function(){r=!0}),1)}}))}return function(){v.forEach(a)}}};var o=0;Prism.hooks.add(\"before-sanity-check\",(function(e){var t=e.element.parentElement;if(u(t)){var i=0;s(\".line-highlight\",t).forEach((function(e){i+=e.textContent.length,e.parentNode.removeChild(e)})),i&&/^(?: \\n)+$/.test(e.code.slice(-i))&&(e.code=e.code.slice(0,-i))}})),Prism.hooks.add(\"complete\",(function e(i){var n=i.element.parentElement;if(u(n)){clearTimeout(o);var r=Prism.plugins.lineNumbers,s=i.plugins&&i.plugins.lineNumbers;l(n,t)&&r&&!s?Prism.hooks.add(\"line-numbers\",e):(Prism.plugins.lineHighlight.highlightLines(n)(),o=setTimeout(c,1))}})),window.addEventListener(\"hashchange\",c),window.addEventListener(\"resize\",(function(){s(\"pre\").filter(u).map((function(e){return Prism.plugins.lineHighlight.highlightLines(e)})).forEach(a)}))}function s(e,t){return Array.prototype.slice.call((t||document).querySelectorAll(e))}function l(e,t){return e.classList.contains(t)}function a(e){e()}function u(e){return!!(e&&/pre/i.test(e.nodeName)&&(e.hasAttribute(\"data-line\")||e.id&&Prism.util.isActive(e,i)))}function c(){var e=location.hash.slice(1);s(\".temporary.line-highlight\").forEach((function(e){e.parentNode.removeChild(e)}));var t=(e.match(/\\.([\\d,-]+)$/)||[,\"\"])[1];if(t&&!document.getElementById(e)){var i=e.slice(0,e.lastIndexOf(\".\")),n=document.getElementById(i);n&&(n.hasAttribute(\"data-line\")||n.setAttribute(\"data-line\",\"\"),Prism.plugins.lineHighlight.highlightLines(n,t,\"temporary \")(),r&&document.querySelector(\".temporary.line-highlight\").scrollIntoView())}}}();\n" +
                    "!function(){if(\"undefined\"!=typeof Prism&&\"undefined\"!=typeof document){var e=\"line-numbers\",n=/\\n(?!$)/g,t=Prism.plugins.lineNumbers={getLine:function(n,t){if(\"PRE\"===n.tagName&&n.classList.contains(e)){var i=n.querySelector(\".line-numbers-rows\");if(i){var r=parseInt(n.getAttribute(\"data-start\"),10)||1,s=r+(i.children.length-1);t<r&&(t=r),t>s&&(t=s);var l=t-r;return i.children[l]}}},resize:function(e){r([e])},assumeViewportIndependence:!0},i=void 0;window.addEventListener(\"resize\",(function(){t.assumeViewportIndependence&&i===window.innerWidth||(i=window.innerWidth,r(Array.prototype.slice.call(document.querySelectorAll(\"pre.line-numbers\"))))})),Prism.hooks.add(\"complete\",(function(t){if(t.code){var i=t.element,s=i.parentNode;if(s&&/pre/i.test(s.nodeName)&&!i.querySelector(\".line-numbers-rows\")&&Prism.util.isActive(i,e)){i.classList.remove(e),s.classList.add(e);var l,o=t.code.match(n),a=o?o.length+1:1,u=new Array(a+1).join(\"<span></span>\");(l=document.createElement(\"span\")).setAttribute(\"aria-hidden\",\"true\"),l.className=\"line-numbers-rows\",l.innerHTML=u,s.hasAttribute(\"data-start\")&&(s.style.counterReset=\"linenumber \"+(parseInt(s.getAttribute(\"data-start\"),10)-1)),t.element.appendChild(l),r([s]),Prism.hooks.run(\"line-numbers\",t)}}})),Prism.hooks.add(\"line-numbers\",(function(e){e.plugins=e.plugins||{},e.plugins.lineNumbers=!0}))}function r(e){if(0!=(e=e.filter((function(e){var n,t=(n=e,n?window.getComputedStyle?getComputedStyle(n):n.currentStyle||null:null)[\"white-space\"];return\"pre-wrap\"===t||\"pre-line\"===t}))).length){var t=e.map((function(e){var t=e.querySelector(\"code\"),i=e.querySelector(\".line-numbers-rows\");if(t&&i){var r=e.querySelector(\".line-numbers-sizer\"),s=t.textContent.split(n);r||((r=document.createElement(\"span\")).className=\"line-numbers-sizer\",t.appendChild(r)),r.innerHTML=\"0\",r.style.display=\"block\";var l=r.getBoundingClientRect().height;return r.innerHTML=\"\",{element:e,lines:s,lineHeights:[],oneLinerHeight:l,sizer:r}}})).filter(Boolean);t.forEach((function(e){var n=e.sizer,t=e.lines,i=e.lineHeights,r=e.oneLinerHeight;i[t.length-1]=void 0,t.forEach((function(e,t){if(e&&e.length>1){var s=n.appendChild(document.createElement(\"span\"));s.style.display=\"block\",s.textContent=e}else i[t]=r}))})),t.forEach((function(e){for(var n=e.sizer,t=e.lineHeights,i=0,r=0;r<t.length;r++)void 0===t[r]&&(t[r]=n.children[i++].getBoundingClientRect().height)})),t.forEach((function(e){var n=e.sizer,t=e.element.querySelector(\".line-numbers-rows\");n.style.display=\"none\",n.innerHTML=\"\",e.lineHeights.forEach((function(e,n){t.children[n].style.height=e+\"px\"}))}))}}}();\n" +
                    "!function(){if(\"undefined\"!=typeof Prism&&\"undefined\"!=typeof document){var n=/<\\/?(?!\\d)[^\\s>\\/=$<%]+(?:\\s(?:\\s*[^\\s>\\/=]+(?:\\s*=\\s*(?:\"[^\"]*\"|'[^']*'|[^\\s'\">=]+(?=[\\s>]))|(?=[\\s/>])))+)?\\s*\\/?>/g,r=/^#?((?:[\\da-f]){3,4}|(?:[\\da-f]{2}){3,4})$/i,o=[function(n){var o=r.exec(n);if(o){for(var s=(n=o[1]).length>=6?2:1,e=n.length/s,t=1==s?1/15:1/255,i=[],a=0;a<e;a++){var c=parseInt(n.substr(a*s,s),16);i.push(c*t)}return 3==e&&i.push(1),\"rgba(\"+i.slice(0,3).map((function(n){return String(Math.round(255*n))})).join(\",\")+\",\"+String(Number(i[3].toFixed(3)))+\")\"}},function(n){var r=(new Option).style;return r.color=n,r.color?n:void 0}];Prism.hooks.add(\"wrap\",(function(r){if(\"color\"===r.type||r.classes.indexOf(\"color\")>=0){for(var s,e=r.content,t=e.split(n).join(\"\"),i=0,a=o.length;i<a&&!s;i++)s=o[i](t);if(!s)return;var c='<span class=\"inline-color-wrapper\"><span class=\"inline-color\" style=\"background-color:'+s+';\"></span></span>';r.content=c+e}}))}}();\n");
            printStreamJS.close();

            File html = new File(System.getProperty("user.dir") + "\\report\\test.html");
            html.createNewFile();
//            PrintStream printStream = new PrintStream(Files.newOutputStream(Paths.get(target)));
            PrintStream printStream = new PrintStream(html);
            printStream.println(stringHtml);
            printStream.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void genMyCSS(String path){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("@charset \"UTF-8\";\n" +
                "\n" +
                "html,body {\n" +
                "    Font-family: Helvetica, Tahoma, Arial, STXihei, “华文细黑”, “Microsoft YaHei”, “微软雅黑”, sans-serif;\n" +
                "    height: 100%;\n" +
                "    background: #f6f7fd;\n" +
                "}\n" +
                "\n" +
                "\n" +
                ".categoryUl {\n" +
                "    flex: 6;\n" +
                "    display: flex;\n" +
                "    flex-direction: row;\n" +
                "    justify-content: flex-start;\n" +
                "    align-items: center;\n" +
                "    list-style-type: none;\n" +
                "}\n" +
                "\n" +
                ".categoryLi{\n" +
                "    margin-left: 5%;\n" +
                "    margin-right: 10%;\n" +
                "    width: 15%;\n" +
                "    height: 10%;\n" +
                "    background: #9a6e3a;\n" +
                "}\n" +
                "\n" +
                "\n" +
                "li a {\n" +
                "    display: block;\n" +
                "    color: #000;\n" +
                "    padding: 12px 15px;\n" +
                "    text-decoration: none;\n" +
                "    margin-left: 30%;\n" +
                "    font-size: 16px;\n" +
                "    font-weight:bold;\n" +
                "\n" +
                "}\n" +
                "\n" +
                ".active{\n" +
                "    background-color: #CCC;\n" +
                "    color: white;\n" +
                "}\n" +
                ".active a{\n" +
                "    color: white;\n" +
                "    font-size: 16px;\n" +
                "    font-weight:bolder;\n" +
                "}\n" +
                "\n" +
                "li:hover{\n" +
                "    background-color:#CCC ;\n" +
                "}\n" +
                "\n" +
                "li a:hover{\n" +
                "    background-color: #CCC;\n" +
                "    color: white;\n" +
                "    font-size: 16px;\n" +
                "    font-weight:bolder;\n" +
                "}\n" +
                "\n" +
                ".topBar{\n" +
                "    background-color: #f6f7fd;\n" +
                "    width: 100%;\n" +
                "    height: 9%;\n" +
                "    position: fixed;\n" +
                "    top: 0;\n" +
                "    display: flex;\n" +
                "    justify-content: space-between;\n" +
                "}\n" +
                "\n" +
                ".helloDiv{\n" +
                "    margin-right: auto;\n" +
                "    margin-left: 45%;\n" +
                "    font-size: 18px;\n" +
                "    font-weight: bolder;\n" +
                "    color: gray;\n" +
                "}\n" +
                "\n" +
                "\n" +
                "\n" +
                ".detailStackBar{\n" +
                "    width: 100%;\n" +
                "    background: rgba(245,242,240,0.55);\n" +
                "    margin-top: 10%;\n" +
                "}\n" +
                "\n" +
                ".totalBar{\n" +
                "    width: 100%;\n" +
                "    height: 18%;\n" +
                "    margin-top: 5%;\n" +
                "    background: white;\n" +
                "}\n" +
                "\n" +
                ".miniTitle {\n" +
                "    font-size: 18px;\n" +
                "    font-weight: bolder;\n" +
                "    margin-left: 1%;\n" +
                "    margin-top: 10px;\n" +
                "}\n" +
                "\n" +
                ".line {\n" +
                "    margin-top: 10px;\n" +
                "    height: 2px;\n" +
                "    border-top: solid #ACC0D8;\n" +
                "}\n" +
                "\n" +
                ".totalDiv {\n" +
                "    display: flex;\n" +
                "    height: 70%;\n" +
                "    overflow: hidden;\n" +
                "}\n" +
                "\n" +
                ".totalDiv1{\n" +
                "    margin-top: 10px;\n" +
                "    margin-left: 10px;\n" +
                "    margin-bottom: 10px;\n" +
                "    width: 23%;\n" +
                "    border-radius: 10px;\n" +
                "    box-shadow: 2px 2px 5px #CCC;\n" +
                "    background: #5aa6ff;\n" +
                "}\n" +
                "\n" +
                ".totalDiv2{\n" +
                "    margin-top: 10px;\n" +
                "    margin-left: 2%;\n" +
                "    margin-bottom: 10px;\n" +
                "    width: 23%;\n" +
                "    border-radius: 10px;\n" +
                "    box-shadow: 2px 2px 5px #CCC;\n" +
                "}\n" +
                "\n" +
                ".fixTotalName{\n" +
                "    font-size: 16px;\n" +
                "    color: white;\n" +
                "    margin-left: 2%;\n" +
                "    margin-top: 2%;\n" +
                "    font-weight: bolder;\n" +
                "    overflow: hidden;\n" +
                "}\n" +
                "\n" +
                "\n" +
                ".totalNum{\n" +
                "    font-style: normal;\n" +
                "    font-size: 25px;\n" +
                "    color: white;\n" +
                "    position: relative;\n" +
                "    top: -10%;\n" +
                "    margin-left: 30%;\n" +
                "    font-weight: bolder;\n" +
                "}\n" +
                "\n" +
                "#content{\n" +
                "    height: 100%;\n" +
                "}\n" +
                "\n" +
                ".pigBar{\n" +
                "    height: 45%;\n" +
                "    width: 100%;\n" +
                "    margin-top: 30px;\n" +
                "    background: white;\n" +
                "    overflow: hidden;\n" +
                "}\n" +
                "\n" +
                ".chartsPig{\n" +
                "    display: flex;\n" +
                "    overflow: hidden;\n" +
                "    height: 85%;\n" +
                "}\n" +
                "\n" +
                ".chartsPig1{\n" +
                "    margin-top: 1%;\n" +
                "    margin-left: 12%;\n" +
                "    height: 100%;\n" +
                "    width: 30%;\n" +
                "    overflow: hidden;\n" +
                "    /*background: #990055;*/\n" +
                "}\n" +
                "\n" +
                ".chartsPig2{\n" +
                "    margin-left: 2%;\n" +
                "    width: 50%;\n" +
                "    height: 100%;\n" +
                "    overflow: hidden;\n" +
                "    /*background: #990055;*/\n" +
                "}\n" +
                "\n" +
                ".detailBar{\n" +
                "    width: 100%;\n" +
                "    background: white;\n" +
                "    overflow: hidden;\n" +
                "    margin-top: 30px;\n" +
                "    /*height: 50%;*/\n" +
                "}\n" +
                "\n" +
                ".detailLi{\n" +
                "    text-decoration:underline;\n" +
                "    color: #5aa6ff;\n" +
                "}\n" +
                "\n" +
                ".tableDiv{\n" +
                "    height: 80%;\n" +
                "    /*overflow: hidden;*/\n" +
                "}\n" +
                "\n" +
                ".fixDetailDiv{\n" +
                "    border-radius: 5px;\n" +
                "    margin-top: 5px;\n" +
                "    height: 40px;\n" +
                "    width: 99%;\n" +
                "    background: #19b394;\n" +
                "    overflow: hidden;\n" +
                "}\n" +
                "\n" +
                ".detailMsgDiv{\n" +
                "    display: flex;\n" +
                "    margin-top: 4px;\n" +
                "    height: 80%;\n" +
                "    margin-left: 70%;\n" +
                "    width: 29.6%;\n" +
                "    background: white;\n" +
                "    overflow: hidden;\n" +
                "\n" +
                "}\n" +
                "\n" +
                ".detailMsg{\n" +
                "    margin-top: 1%;\n" +
                "    width: 20%;\n" +
                "    font-size: 14px;\n" +
                "    font-weight: lighter;\n" +
                "}\n" +
                "\n" +
                ".detailMsgTable{\n" +
                "    width: 99%;\n" +
                "    border-collapse: collapse;\n" +
                "    border:1px solid #CCC;\n" +
                "}\n" +
                "\n" +
                ".tableTDA{\n" +
                "    width: 5%;\n" +
                "}\n" +
                "\n" +
                ".tableTDB{\n" +
                "    width: 10%;\n" +
                "}\n" +
                "\n" +
                ".blank{\n" +
                "    /*position: relative;*/\n" +
                "    background: #ffc068;\n" +
                "    /*margin-top: 30%;*/\n" +
                "    height: 20px;\n" +
                "}\n" +
                " /*滚动条宽度 */\n" +
                "::-webkit-scrollbar {\n" +
                "    width: 10px;\n" +
                "    background-color: transparent;\n" +
                "}\n" +
                "\n" +
                "::-webkit-scrollbar {\n" +
                "    display: none;\n" +
                "}\n" +
                "\n" +
                "/* 滚动条颜色 */\n" +
                "#tbodyResult ::-webkit-scrollbar-thumb {\n" +
                "    background-color: gray;\n" +
                "}\n" +
                "\n" +
                "table th, table td {\n" +
                "    height: 30px;\n" +
                "    text-align: center;\n" +
                "    border: 1px solid gray;\n" +
                "}\n" +
                "\n" +
                "#tbodyResult {\n" +
                "    display: block;\n" +
                "    width: calc(100% ); /*这里的8px是滚动条的宽度*/\n" +
                "    /*max-height: 300px;*/\n" +
                "    overflow-y: auto;\n" +
                "    -webkit-overflow-scrolling: touch;\n" +
                "    overflow-x: hidden;\n" +
                "}\n" +
                "\n" +
                ".tableHead {\n" +
                "    box-sizing: border-box;\n" +
                "    table-layout: fixed;\n" +
                "    display: table;\n" +
                "    width: 100%;\n" +
                "    color: black;\n" +
                "    background-color: #EEE;\n" +
                "}\n" +
                "\n" +
                "table tbody tr {\n" +
                "    box-sizing: border-box;\n" +
                "    table-layout: fixed;\n" +
                "    display: table;\n" +
                "    width: 100%;\n" +
                "    background: #FFFFFF;\n" +
                "}\n" +
                "\n" +
                "table tbody tr:nth-of-type(odd) {\n" +
                "    background: #EEE;\n" +
                "}\n" +
                "\n" +
                "table tbody tr:nth-of-type(even) {\n" +
                "    background: #FFF;\n" +
                "}\n");

        try {
            File cssFile = new File(path + File.separator + "css");
            if (!cssFile.exists()) {
                cssFile.mkdirs();
            }
            File prismJS = new File(path + File.separator + "css" + File.separator + "my.css");
            prismJS.createNewFile();
            PrintStream printStreamJS = new PrintStream(prismJS);
            printStreamJS.println(stringBuilder.toString());
            printStreamJS.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    // path为被测项目的路径 project/justinStr-report 这个目录确定存在
    private static void genPrimCSS(String path){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("/* PrismJS 1.28.0\n" +
                "https://prismjs.com/download.html#themes=prism&languages=markup+css+clike+javascript+css-extras+java&plugins=line-highlight+line-numbers+inline-color */\n" +
                "code[class*=language-],pre[class*=language-]{color:#000;background:0 0;text-shadow:0 1px #fff;font-family:Consolas,Monaco,'Andale Mono','Ubuntu Mono',monospace;font-size:1em;text-align:left;white-space:pre;word-spacing:normal;word-break:normal;word-wrap:normal;line-height:1.5;-moz-tab-size:4;-o-tab-size:4;tab-size:4;-webkit-hyphens:none;-moz-hyphens:none;-ms-hyphens:none;hyphens:none}code[class*=language-] ::-moz-selection,code[class*=language-]::-moz-selection,pre[class*=language-] ::-moz-selection,pre[class*=language-]::-moz-selection{text-shadow:none;background:#b3d4fc}code[class*=language-] ::selection,code[class*=language-]::selection,pre[class*=language-] ::selection,pre[class*=language-]::selection{text-shadow:none;background:#b3d4fc}@media print{code[class*=language-],pre[class*=language-]{text-shadow:none}}pre[class*=language-]{padding:1em;margin:.5em 0;overflow:auto}:not(pre)>code[class*=language-],pre[class*=language-]{background:#f5f2f0}:not(pre)>code[class*=language-]{padding:.1em;border-radius:.3em;white-space:normal}.token.cdata,.token.comment,.token.doctype,.token.prolog{color:#708090}.token.punctuation{color:#999}.token.namespace{opacity:.7}.token.boolean,.token.constant,.token.deleted,.token.number,.token.property,.token.symbol,.token.tag{color:#905}.token.attr-name,.token.builtin,.token.char,.token.inserted,.token.selector,.token.string{color:#690}.language-css .token.string,.style .token.string,.token.entity,.token.operator,.token.url{color:#9a6e3a;background:hsla(0,0%,100%,.5)}.token.atrule,.token.attr-value,.token.keyword{color:#07a}.token.class-name,.token.function{color:#dd4a68}.token.important,.token.regex,.token.variable{color:#e90}.token.bold,.token.important{font-weight:700}.token.italic{font-style:italic}.token.entity{cursor:help}\n" +
                "pre[data-line]{position:relative;padding:1em 0 1em 3em}.line-highlight{position:absolute;left:0;right:0;padding:inherit 0;margin-top:1em;background:hsla(24,20%,50%,.08);background:linear-gradient(to right,hsla(24,20%,50%,.1) 70%,hsla(24,20%,50%,0));pointer-events:none;line-height:inherit;white-space:pre}@media print{.line-highlight{-webkit-print-color-adjust:exact;color-adjust:exact}}.line-highlight:before,.line-highlight[data-end]:after{content:attr(data-start);position:absolute;top:.4em;left:.6em;min-width:1em;padding:0 .5em;background-color:hsla(24,20%,50%,.4);color:#f4f1ef;font:bold 65%/1.5 sans-serif;text-align:center;vertical-align:.3em;border-radius:999px;text-shadow:none;box-shadow:0 1px #fff}.line-highlight[data-end]:after{content:attr(data-end);top:auto;bottom:.4em}.line-numbers .line-highlight:after,.line-numbers .line-highlight:before{content:none}pre[id].linkable-line-numbers span.line-numbers-rows{pointer-events:all}pre[id].linkable-line-numbers span.line-numbers-rows>span:before{cursor:pointer}pre[id].linkable-line-numbers span.line-numbers-rows>span:hover:before{background-color:rgba(128,128,128,.2)}\n" +
                "pre[class*=language-].line-numbers{position:relative;padding-left:3.8em;counter-reset:linenumber}pre[class*=language-].line-numbers>code{position:relative;white-space:inherit}.line-numbers .line-numbers-rows{position:absolute;pointer-events:none;top:0;font-size:100%;left:-3.8em;width:3em;letter-spacing:-1px;border-right:1px solid #999;-webkit-user-select:none;-moz-user-select:none;-ms-user-select:none;user-select:none}.line-numbers-rows>span{display:block;counter-increment:linenumber}.line-numbers-rows>span:before{content:counter(linenumber);color:#999;display:block;padding-right:.8em;text-align:right}\n" +
                "span.inline-color-wrapper{background:url(data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyIDIiPjxwYXRoIGZpbGw9ImdyYXkiIGQ9Ik0wIDBoMnYySDB6Ii8+PHBhdGggZmlsbD0id2hpdGUiIGQ9Ik0wIDBoMXYxSDB6TTEgMWgxdjFIMXoiLz48L3N2Zz4=);background-position:center;background-size:110%;display:inline-block;height:1.333ch;width:1.333ch;margin:0 .333ch;box-sizing:border-box;border:1px solid #fff;outline:1px solid rgba(0,0,0,.5);overflow:hidden}span.inline-color{display:block;height:120%;width:120%}\n");

        try {
            File jsFile = new File(path + File.separator + "css");
            if (!jsFile.exists()) {
                jsFile.mkdirs();
            }
            File prismJS = new File(path + File.separator + "css" + File.separator + "prism.css");
            prismJS.createNewFile();
            PrintStream printStreamJS = new PrintStream(prismJS);
            printStreamJS.println(stringBuilder.toString());
            printStreamJS.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void genPrimJS(String path){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("/* PrismJS 1.28.0\n" +
                "https://prismjs.com/download.html#themes=prism&languages=markup+css+clike+javascript+css-extras+java&plugins=line-highlight+line-numbers+inline-color */\n" +
                "var _self=\"undefined\"!=typeof window?window:\"undefined\"!=typeof WorkerGlobalScope&&self instanceof WorkerGlobalScope?self:{},Prism=function(e){var n=/(?:^|\\s)lang(?:uage)?-([\\w-]+)(?=\\s|$)/i,t=0,r={},a={manual:e.Prism&&e.Prism.manual,disableWorkerMessageHandler:e.Prism&&e.Prism.disableWorkerMessageHandler,util:{encode:function e(n){return n instanceof i?new i(n.type,e(n.content),n.alias):Array.isArray(n)?n.map(e):n.replace(/&/g,\"&amp;\").replace(/</g,\"&lt;\").replace(/\\u00a0/g,\" \")},type:function(e){return Object.prototype.toString.call(e).slice(8,-1)},objId:function(e){return e.__id||Object.defineProperty(e,\"__id\",{value:++t}),e.__id},clone:function e(n,t){var r,i;switch(t=t||{},a.util.type(n)){case\"Object\":if(i=a.util.objId(n),t[i])return t[i];for(var l in r={},t[i]=r,n)n.hasOwnProperty(l)&&(r[l]=e(n[l],t));return r;case\"Array\":return i=a.util.objId(n),t[i]?t[i]:(r=[],t[i]=r,n.forEach((function(n,a){r[a]=e(n,t)})),r);default:return n}},getLanguage:function(e){for(;e;){var t=n.exec(e.className);if(t)return t[1].toLowerCase();e=e.parentElement}return\"none\"},setLanguage:function(e,t){e.className=e.className.replace(RegExp(n,\"gi\"),\"\"),e.classList.add(\"language-\"+t)},currentScript:function(){if(\"undefined\"==typeof document)return null;if(\"currentScript\"in document)return document.currentScript;try{throw new Error}catch(r){var e=(/at [^(\\r\\n]*\\((.*):[^:]+:[^:]+\\)$/i.exec(r.stack)||[])[1];if(e){var n=document.getElementsByTagName(\"script\");for(var t in n)if(n[t].src==e)return n[t]}return null}},isActive:function(e,n,t){for(var r=\"no-\"+n;e;){var a=e.classList;if(a.contains(n))return!0;if(a.contains(r))return!1;e=e.parentElement}return!!t}},languages:{plain:r,plaintext:r,text:r,txt:r,extend:function(e,n){var t=a.util.clone(a.languages[e]);for(var r in n)t[r]=n[r];return t},insertBefore:function(e,n,t,r){var i=(r=r||a.languages)[e],l={};for(var o in i)if(i.hasOwnProperty(o)){if(o==n)for(var s in t)t.hasOwnProperty(s)&&(l[s]=t[s]);t.hasOwnProperty(o)||(l[o]=i[o])}var u=r[e];return r[e]=l,a.languages.DFS(a.languages,(function(n,t){t===u&&n!=e&&(this[n]=l)})),l},DFS:function e(n,t,r,i){i=i||{};var l=a.util.objId;for(var o in n)if(n.hasOwnProperty(o)){t.call(n,o,n[o],r||o);var s=n[o],u=a.util.type(s);\"Object\"!==u||i[l(s)]?\"Array\"!==u||i[l(s)]||(i[l(s)]=!0,e(s,t,o,i)):(i[l(s)]=!0,e(s,t,null,i))}}},plugins:{},highlightAll:function(e,n){a.highlightAllUnder(document,e,n)},highlightAllUnder:function(e,n,t){var r={callback:t,container:e,selector:'code[class*=\"language-\"], [class*=\"language-\"] code, code[class*=\"lang-\"], [class*=\"lang-\"] code'};a.hooks.run(\"before-highlightall\",r),r.elements=Array.prototype.slice.apply(r.container.querySelectorAll(r.selector)),a.hooks.run(\"before-all-elements-highlight\",r);for(var i,l=0;i=r.elements[l++];)a.highlightElement(i,!0===n,r.callback)},highlightElement:function(n,t,r){var i=a.util.getLanguage(n),l=a.languages[i];a.util.setLanguage(n,i);var o=n.parentElement;o&&\"pre\"===o.nodeName.toLowerCase()&&a.util.setLanguage(o,i);var s={element:n,language:i,grammar:l,code:n.textContent};function u(e){s.highlightedCode=e,a.hooks.run(\"before-insert\",s),s.element.innerHTML=s.highlightedCode,a.hooks.run(\"after-highlight\",s),a.hooks.run(\"complete\",s),r&&r.call(s.element)}if(a.hooks.run(\"before-sanity-check\",s),(o=s.element.parentElement)&&\"pre\"===o.nodeName.toLowerCase()&&!o.hasAttribute(\"tabindex\")&&o.setAttribute(\"tabindex\",\"0\"),!s.code)return a.hooks.run(\"complete\",s),void(r&&r.call(s.element));if(a.hooks.run(\"before-highlight\",s),s.grammar)if(t&&e.Worker){var c=new Worker(a.filename);c.onmessage=function(e){u(e.data)},c.postMessage(JSON.stringify({language:s.language,code:s.code,immediateClose:!0}))}else u(a.highlight(s.code,s.grammar,s.language));else u(a.util.encode(s.code))},highlight:function(e,n,t){var r={code:e,grammar:n,language:t};if(a.hooks.run(\"before-tokenize\",r),!r.grammar)throw new Error('The language \"'+r.language+'\" has no grammar.');return r.tokens=a.tokenize(r.code,r.grammar),a.hooks.run(\"after-tokenize\",r),i.stringify(a.util.encode(r.tokens),r.language)},tokenize:function(e,n){var t=n.rest;if(t){for(var r in t)n[r]=t[r];delete n.rest}var a=new s;return u(a,a.head,e),o(e,a,n,a.head,0),function(e){for(var n=[],t=e.head.next;t!==e.tail;)n.push(t.value),t=t.next;return n}(a)},hooks:{all:{},add:function(e,n){var t=a.hooks.all;t[e]=t[e]||[],t[e].push(n)},run:function(e,n){var t=a.hooks.all[e];if(t&&t.length)for(var r,i=0;r=t[i++];)r(n)}},Token:i};function i(e,n,t,r){this.type=e,this.content=n,this.alias=t,this.length=0|(r||\"\").length}function l(e,n,t,r){e.lastIndex=n;var a=e.exec(t);if(a&&r&&a[1]){var i=a[1].length;a.index+=i,a[0]=a[0].slice(i)}return a}function o(e,n,t,r,s,g){for(var f in t)if(t.hasOwnProperty(f)&&t[f]){var h=t[f];h=Array.isArray(h)?h:[h];for(var d=0;d<h.length;++d){if(g&&g.cause==f+\",\"+d)return;var v=h[d],p=v.inside,m=!!v.lookbehind,y=!!v.greedy,k=v.alias;if(y&&!v.pattern.global){var x=v.pattern.toString().match(/[imsuy]*$/)[0];v.pattern=RegExp(v.pattern.source,x+\"g\")}for(var b=v.pattern||v,w=r.next,A=s;w!==n.tail&&!(g&&A>=g.reach);A+=w.value.length,w=w.next){var E=w.value;if(n.length>e.length)return;if(!(E instanceof i)){var P,L=1;if(y){if(!(P=l(b,A,e,m))||P.index>=e.length)break;var S=P.index,O=P.index+P[0].length,j=A;for(j+=w.value.length;S>=j;)j+=(w=w.next).value.length;if(A=j-=w.value.length,w.value instanceof i)continue;for(var C=w;C!==n.tail&&(j<O||\"string\"==typeof C.value);C=C.next)L++,j+=C.value.length;L--,E=e.slice(A,j),P.index-=A}else if(!(P=l(b,0,E,m)))continue;S=P.index;var N=P[0],_=E.slice(0,S),M=E.slice(S+N.length),W=A+E.length;g&&W>g.reach&&(g.reach=W);var z=w.prev;if(_&&(z=u(n,z,_),A+=_.length),c(n,z,L),w=u(n,z,new i(f,p?a.tokenize(N,p):N,k,N)),M&&u(n,w,M),L>1){var I={cause:f+\",\"+d,reach:W};o(e,n,t,w.prev,A,I),g&&I.reach>g.reach&&(g.reach=I.reach)}}}}}}function s(){var e={value:null,prev:null,next:null},n={value:null,prev:e,next:null};e.next=n,this.head=e,this.tail=n,this.length=0}function u(e,n,t){var r=n.next,a={value:t,prev:n,next:r};return n.next=a,r.prev=a,e.length++,a}function c(e,n,t){for(var r=n.next,a=0;a<t&&r!==e.tail;a++)r=r.next;n.next=r,r.prev=n,e.length-=a}if(e.Prism=a,i.stringify=function e(n,t){if(\"string\"==typeof n)return n;if(Array.isArray(n)){var r=\"\";return n.forEach((function(n){r+=e(n,t)})),r}var i={type:n.type,content:e(n.content,t),tag:\"span\",classes:[\"token\",n.type],attributes:{},language:t},l=n.alias;l&&(Array.isArray(l)?Array.prototype.push.apply(i.classes,l):i.classes.push(l)),a.hooks.run(\"wrap\",i);var o=\"\";for(var s in i.attributes)o+=\" \"+s+'=\"'+(i.attributes[s]||\"\").replace(/\"/g,\"&quot;\")+'\"';return\"<\"+i.tag+' class=\"'+i.classes.join(\" \")+'\"'+o+\">\"+i.content+\"</\"+i.tag+\">\"},!e.document)return e.addEventListener?(a.disableWorkerMessageHandler||e.addEventListener(\"message\",(function(n){var t=JSON.parse(n.data),r=t.language,i=t.code,l=t.immediateClose;e.postMessage(a.highlight(i,a.languages[r],r)),l&&e.close()}),!1),a):a;var g=a.util.currentScript();function f(){a.manual||a.highlightAll()}if(g&&(a.filename=g.src,g.hasAttribute(\"data-manual\")&&(a.manual=!0)),!a.manual){var h=document.readyState;\"loading\"===h||\"interactive\"===h&&g&&g.defer?document.addEventListener(\"DOMContentLoaded\",f):window.requestAnimationFrame?window.requestAnimationFrame(f):window.setTimeout(f,16)}return a}(_self);\"undefined\"!=typeof module&&module.exports&&(module.exports=Prism),\"undefined\"!=typeof global&&(global.Prism=Prism);\n" +
                "Prism.languages.markup={comment:{pattern:/<!--(?:(?!<!--)[\\s\\S])*?-->/,greedy:!0},prolog:{pattern:/<\\?[\\s\\S]+?\\?>/,greedy:!0},doctype:{pattern:/<!DOCTYPE(?:[^>\"'[\\]]|\"[^\"]*\"|'[^']*')+(?:\\[(?:[^<\"'\\]]|\"[^\"]*\"|'[^']*'|<(?!!--)|<!--(?:[^-]|-(?!->))*-->)*\\]\\s*)?>/i,greedy:!0,inside:{\"internal-subset\":{pattern:/(^[^\\[]*\\[)[\\s\\S]+(?=\\]>$)/,lookbehind:!0,greedy:!0,inside:null},string:{pattern:/\"[^\"]*\"|'[^']*'/,greedy:!0},punctuation:/^<!|>$|[[\\]]/,\"doctype-tag\":/^DOCTYPE/i,name:/[^\\s<>'\"]+/}},cdata:{pattern:/<!\\[CDATA\\[[\\s\\S]*?\\]\\]>/i,greedy:!0},tag:{pattern:/<\\/?(?!\\d)[^\\s>\\/=$<%]+(?:\\s(?:\\s*[^\\s>\\/=]+(?:\\s*=\\s*(?:\"[^\"]*\"|'[^']*'|[^\\s'\">=]+(?=[\\s>]))|(?=[\\s/>])))+)?\\s*\\/?>/,greedy:!0,inside:{tag:{pattern:/^<\\/?[^\\s>\\/]+/,inside:{punctuation:/^<\\/?/,namespace:/^[^\\s>\\/:]+:/}},\"special-attr\":[],\"attr-value\":{pattern:/=\\s*(?:\"[^\"]*\"|'[^']*'|[^\\s'\">=]+)/,inside:{punctuation:[{pattern:/^=/,alias:\"attr-equals\"},{pattern:/^(\\s*)[\"']|[\"']$/,lookbehind:!0}]}},punctuation:/\\/?>/,\"attr-name\":{pattern:/[^\\s>\\/]+/,inside:{namespace:/^[^\\s>\\/:]+:/}}}},entity:[{pattern:/&[\\da-z]{1,8};/i,alias:\"named-entity\"},/&#x?[\\da-f]{1,8};/i]},Prism.languages.markup.tag.inside[\"attr-value\"].inside.entity=Prism.languages.markup.entity,Prism.languages.markup.doctype.inside[\"internal-subset\"].inside=Prism.languages.markup,Prism.hooks.add(\"wrap\",(function(a){\"entity\"===a.type&&(a.attributes.title=a.content.replace(/&amp;/,\"&\"))})),Object.defineProperty(Prism.languages.markup.tag,\"addInlined\",{value:function(a,e){var s={};s[\"language-\"+e]={pattern:/(^<!\\[CDATA\\[)[\\s\\S]+?(?=\\]\\]>$)/i,lookbehind:!0,inside:Prism.languages[e]},s.cdata=/^<!\\[CDATA\\[|\\]\\]>$/i;var t={\"included-cdata\":{pattern:/<!\\[CDATA\\[[\\s\\S]*?\\]\\]>/i,inside:s}};t[\"language-\"+e]={pattern:/[\\s\\S]+/,inside:Prism.languages[e]};var n={};n[a]={pattern:RegExp(\"(<__[^>]*>)(?:<!\\\\[CDATA\\\\[(?:[^\\\\]]|\\\\](?!\\\\]>))*\\\\]\\\\]>|(?!<!\\\\[CDATA\\\\[)[^])*?(?=</__>)\".replace(/__/g,(function(){return a})),\"i\"),lookbehind:!0,greedy:!0,inside:t},Prism.languages.insertBefore(\"markup\",\"cdata\",n)}}),Object.defineProperty(Prism.languages.markup.tag,\"addAttribute\",{value:function(a,e){Prism.languages.markup.tag.inside[\"special-attr\"].push({pattern:RegExp(\"(^|[\\\"'\\\\s])(?:\"+a+\")\\\\s*=\\\\s*(?:\\\"[^\\\"]*\\\"|'[^']*'|[^\\\\s'\\\">=]+(?=[\\\\s>]))\",\"i\"),lookbehind:!0,inside:{\"attr-name\":/^[^\\s=]+/,\"attr-value\":{pattern:/=[\\s\\S]+/,inside:{value:{pattern:/(^=\\s*([\"']|(?![\"'])))\\S[\\s\\S]*(?=\\2$)/,lookbehind:!0,alias:[e,\"language-\"+e],inside:Prism.languages[e]},punctuation:[{pattern:/^=/,alias:\"attr-equals\"},/\"|'/]}}}})}}),Prism.languages.html=Prism.languages.markup,Prism.languages.mathml=Prism.languages.markup,Prism.languages.svg=Prism.languages.markup,Prism.languages.xml=Prism.languages.extend(\"markup\",{}),Prism.languages.ssml=Prism.languages.xml,Prism.languages.atom=Prism.languages.xml,Prism.languages.rss=Prism.languages.xml;\n" +
                "!function(s){var e=/(?:\"(?:\\\\(?:\\r\\n|[\\s\\S])|[^\"\\\\\\r\\n])*\"|'(?:\\\\(?:\\r\\n|[\\s\\S])|[^'\\\\\\r\\n])*')/;s.languages.css={comment:/\\/\\*[\\s\\S]*?\\*\\//,atrule:{pattern:RegExp(\"@[\\\\w-](?:[^;{\\\\s\\\"']|\\\\s+(?!\\\\s)|\"+e.source+\")*?(?:;|(?=\\\\s*\\\\{))\"),inside:{rule:/^@[\\w-]+/,\"selector-function-argument\":{pattern:/(\\bselector\\s*\\(\\s*(?![\\s)]))(?:[^()\\s]|\\s+(?![\\s)])|\\((?:[^()]|\\([^()]*\\))*\\))+(?=\\s*\\))/,lookbehind:!0,alias:\"selector\"},keyword:{pattern:/(^|[^\\w-])(?:and|not|only|or)(?![\\w-])/,lookbehind:!0}}},url:{pattern:RegExp(\"\\\\burl\\\\((?:\"+e.source+\"|(?:[^\\\\\\\\\\r\\n()\\\"']|\\\\\\\\[^])*)\\\\)\",\"i\"),greedy:!0,inside:{function:/^url/i,punctuation:/^\\(|\\)$/,string:{pattern:RegExp(\"^\"+e.source+\"$\"),alias:\"url\"}}},selector:{pattern:RegExp(\"(^|[{}\\\\s])[^{}\\\\s](?:[^{};\\\"'\\\\s]|\\\\s+(?![\\\\s{])|\"+e.source+\")*(?=\\\\s*\\\\{)\"),lookbehind:!0},string:{pattern:e,greedy:!0},property:{pattern:/(^|[^-\\w\\xA0-\\uFFFF])(?!\\s)[-_a-z\\xA0-\\uFFFF](?:(?!\\s)[-\\w\\xA0-\\uFFFF])*(?=\\s*:)/i,lookbehind:!0},important:/!important\\b/i,function:{pattern:/(^|[^-a-z0-9])[-a-z0-9]+(?=\\()/i,lookbehind:!0},punctuation:/[(){};:,]/},s.languages.css.atrule.inside.rest=s.languages.css;var t=s.languages.markup;t&&(t.tag.addInlined(\"style\",\"css\"),t.tag.addAttribute(\"style\",\"css\"))}(Prism);\n" +
                "Prism.languages.clike={comment:[{pattern:/(^|[^\\\\])\\/\\*[\\s\\S]*?(?:\\*\\/|$)/,lookbehind:!0,greedy:!0},{pattern:/(^|[^\\\\:])\\/\\/.*/,lookbehind:!0,greedy:!0}],string:{pattern:/([\"'])(?:\\\\(?:\\r\\n|[\\s\\S])|(?!\\1)[^\\\\\\r\\n])*\\1/,greedy:!0},\"class-name\":{pattern:/(\\b(?:class|extends|implements|instanceof|interface|new|trait)\\s+|\\bcatch\\s+\\()[\\w.\\\\]+/i,lookbehind:!0,inside:{punctuation:/[.\\\\]/}},keyword:/\\b(?:break|catch|continue|do|else|finally|for|function|if|in|instanceof|new|null|return|throw|try|while)\\b/,boolean:/\\b(?:false|true)\\b/,function:/\\b\\w+(?=\\()/,number:/\\b0x[\\da-f]+\\b|(?:\\b\\d+(?:\\.\\d*)?|\\B\\.\\d+)(?:e[+-]?\\d+)?/i,operator:/[<>]=?|[!=]=?=?|--?|\\+\\+?|&&?|\\|\\|?|[?*/~^%]/,punctuation:/[{}[\\];(),.:]/};\n" +
                "Prism.languages.javascript=Prism.languages.extend(\"clike\",{\"class-name\":[Prism.languages.clike[\"class-name\"],{pattern:/(^|[^$\\w\\xA0-\\uFFFF])(?!\\s)[_$A-Z\\xA0-\\uFFFF](?:(?!\\s)[$\\w\\xA0-\\uFFFF])*(?=\\.(?:constructor|prototype))/,lookbehind:!0}],keyword:[{pattern:/((?:^|\\})\\s*)catch\\b/,lookbehind:!0},{pattern:/(^|[^.]|\\.\\.\\.\\s*)\\b(?:as|assert(?=\\s*\\{)|async(?=\\s*(?:function\\b|\\(|[$\\w\\xA0-\\uFFFF]|$))|await|break|case|class|const|continue|debugger|default|delete|do|else|enum|export|extends|finally(?=\\s*(?:\\{|$))|for|from(?=\\s*(?:['\"]|$))|function|(?:get|set)(?=\\s*(?:[#\\[$\\w\\xA0-\\uFFFF]|$))|if|implements|import|in|instanceof|interface|let|new|null|of|package|private|protected|public|return|static|super|switch|this|throw|try|typeof|undefined|var|void|while|with|yield)\\b/,lookbehind:!0}],function:/#?(?!\\s)[_$a-zA-Z\\xA0-\\uFFFF](?:(?!\\s)[$\\w\\xA0-\\uFFFF])*(?=\\s*(?:\\.\\s*(?:apply|bind|call)\\s*)?\\()/,number:{pattern:RegExp(\"(^|[^\\\\w$])(?:NaN|Infinity|0[bB][01]+(?:_[01]+)*n?|0[oO][0-7]+(?:_[0-7]+)*n?|0[xX][\\\\dA-Fa-f]+(?:_[\\\\dA-Fa-f]+)*n?|\\\\d+(?:_\\\\d+)*n|(?:\\\\d+(?:_\\\\d+)*(?:\\\\.(?:\\\\d+(?:_\\\\d+)*)?)?|\\\\.\\\\d+(?:_\\\\d+)*)(?:[Ee][+-]?\\\\d+(?:_\\\\d+)*)?)(?![\\\\w$])\"),lookbehind:!0},operator:/--|\\+\\+|\\*\\*=?|=>|&&=?|\\|\\|=?|[!=]==|<<=?|>>>?=?|[-+*/%&|^!=<>]=?|\\.{3}|\\?\\?=?|\\?\\.?|[~:]/}),Prism.languages.javascript[\"class-name\"][0].pattern=/(\\b(?:class|extends|implements|instanceof|interface|new)\\s+)[\\w.\\\\]+/,Prism.languages.insertBefore(\"javascript\",\"keyword\",{regex:{pattern:RegExp(\"((?:^|[^$\\\\w\\\\xA0-\\\\uFFFF.\\\"'\\\\])\\\\s]|\\\\b(?:return|yield))\\\\s*)/(?:(?:\\\\[(?:[^\\\\]\\\\\\\\\\r\\n]|\\\\\\\\.)*\\\\]|\\\\\\\\.|[^/\\\\\\\\\\\\[\\r\\n])+/[dgimyus]{0,7}|(?:\\\\[(?:[^[\\\\]\\\\\\\\\\r\\n]|\\\\\\\\.|\\\\[(?:[^[\\\\]\\\\\\\\\\r\\n]|\\\\\\\\.|\\\\[(?:[^[\\\\]\\\\\\\\\\r\\n]|\\\\\\\\.)*\\\\])*\\\\])*\\\\]|\\\\\\\\.|[^/\\\\\\\\\\\\[\\r\\n])+/[dgimyus]{0,7}v[dgimyus]{0,7})(?=(?:\\\\s|/\\\\*(?:[^*]|\\\\*(?!/))*\\\\*/)*(?:$|[\\r\\n,.;:})\\\\]]|//))\"),lookbehind:!0,greedy:!0,inside:{\"regex-source\":{pattern:/^(\\/)[\\s\\S]+(?=\\/[a-z]*$)/,lookbehind:!0,alias:\"language-regex\",inside:Prism.languages.regex},\"regex-delimiter\":/^\\/|\\/$/,\"regex-flags\":/^[a-z]+$/}},\"function-variable\":{pattern:/#?(?!\\s)[_$a-zA-Z\\xA0-\\uFFFF](?:(?!\\s)[$\\w\\xA0-\\uFFFF])*(?=\\s*[=:]\\s*(?:async\\s*)?(?:\\bfunction\\b|(?:\\((?:[^()]|\\([^()]*\\))*\\)|(?!\\s)[_$a-zA-Z\\xA0-\\uFFFF](?:(?!\\s)[$\\w\\xA0-\\uFFFF])*)\\s*=>))/,alias:\"function\"},parameter:[{pattern:/(function(?:\\s+(?!\\s)[_$a-zA-Z\\xA0-\\uFFFF](?:(?!\\s)[$\\w\\xA0-\\uFFFF])*)?\\s*\\(\\s*)(?!\\s)(?:[^()\\s]|\\s+(?![\\s)])|\\([^()]*\\))+(?=\\s*\\))/,lookbehind:!0,inside:Prism.languages.javascript},{pattern:/(^|[^$\\w\\xA0-\\uFFFF])(?!\\s)[_$a-z\\xA0-\\uFFFF](?:(?!\\s)[$\\w\\xA0-\\uFFFF])*(?=\\s*=>)/i,lookbehind:!0,inside:Prism.languages.javascript},{pattern:/(\\(\\s*)(?!\\s)(?:[^()\\s]|\\s+(?![\\s)])|\\([^()]*\\))+(?=\\s*\\)\\s*=>)/,lookbehind:!0,inside:Prism.languages.javascript},{pattern:/((?:\\b|\\s|^)(?!(?:as|async|await|break|case|catch|class|const|continue|debugger|default|delete|do|else|enum|export|extends|finally|for|from|function|get|if|implements|import|in|instanceof|interface|let|new|null|of|package|private|protected|public|return|set|static|super|switch|this|throw|try|typeof|undefined|var|void|while|with|yield)(?![$\\w\\xA0-\\uFFFF]))(?:(?!\\s)[_$a-zA-Z\\xA0-\\uFFFF](?:(?!\\s)[$\\w\\xA0-\\uFFFF])*\\s*)\\(\\s*|\\]\\s*\\(\\s*)(?!\\s)(?:[^()\\s]|\\s+(?![\\s)])|\\([^()]*\\))+(?=\\s*\\)\\s*\\{)/,lookbehind:!0,inside:Prism.languages.javascript}],constant:/\\b[A-Z](?:[A-Z_]|\\dx?)*\\b/}),Prism.languages.insertBefore(\"javascript\",\"string\",{hashbang:{pattern:/^#!.*/,greedy:!0,alias:\"comment\"},\"template-string\":{pattern:/`(?:\\\\[\\s\\S]|\\$\\{(?:[^{}]|\\{(?:[^{}]|\\{[^}]*\\})*\\})+\\}|(?!\\$\\{)[^\\\\`])*`/,greedy:!0,inside:{\"template-punctuation\":{pattern:/^`|`$/,alias:\"string\"},interpolation:{pattern:/((?:^|[^\\\\])(?:\\\\{2})*)\\$\\{(?:[^{}]|\\{(?:[^{}]|\\{[^}]*\\})*\\})+\\}/,lookbehind:!0,inside:{\"interpolation-punctuation\":{pattern:/^\\$\\{|\\}$/,alias:\"punctuation\"},rest:Prism.languages.javascript}},string:/[\\s\\S]+/}},\"string-property\":{pattern:/((?:^|[,{])[ \\t]*)([\"'])(?:\\\\(?:\\r\\n|[\\s\\S])|(?!\\2)[^\\\\\\r\\n])*\\2(?=\\s*:)/m,lookbehind:!0,greedy:!0,alias:\"property\"}}),Prism.languages.insertBefore(\"javascript\",\"operator\",{\"literal-property\":{pattern:/((?:^|[,{])[ \\t]*)(?!\\s)[_$a-zA-Z\\xA0-\\uFFFF](?:(?!\\s)[$\\w\\xA0-\\uFFFF])*(?=\\s*:)/m,lookbehind:!0,alias:\"property\"}}),Prism.languages.markup&&(Prism.languages.markup.tag.addInlined(\"script\",\"javascript\"),Prism.languages.markup.tag.addAttribute(\"on(?:abort|blur|change|click|composition(?:end|start|update)|dblclick|error|focus(?:in|out)?|key(?:down|up)|load|mouse(?:down|enter|leave|move|out|over|up)|reset|resize|scroll|select|slotchange|submit|unload|wheel)\",\"javascript\")),Prism.languages.js=Prism.languages.javascript;\n" +
                "!function(e){var a,n=/(\"|')(?:\\\\(?:\\r\\n|[\\s\\S])|(?!\\1)[^\\\\\\r\\n])*\\1/;e.languages.css.selector={pattern:e.languages.css.selector.pattern,lookbehind:!0,inside:a={\"pseudo-element\":/:(?:after|before|first-letter|first-line|selection)|::[-\\w]+/,\"pseudo-class\":/:[-\\w]+/,class:/\\.[-\\w]+/,id:/#[-\\w]+/,attribute:{pattern:RegExp(\"\\\\[(?:[^[\\\\]\\\"']|\"+n.source+\")*\\\\]\"),greedy:!0,inside:{punctuation:/^\\[|\\]$/,\"case-sensitivity\":{pattern:/(\\s)[si]$/i,lookbehind:!0,alias:\"keyword\"},namespace:{pattern:/^(\\s*)(?:(?!\\s)[-*\\w\\xA0-\\uFFFF])*\\|(?!=)/,lookbehind:!0,inside:{punctuation:/\\|$/}},\"attr-name\":{pattern:/^(\\s*)(?:(?!\\s)[-\\w\\xA0-\\uFFFF])+/,lookbehind:!0},\"attr-value\":[n,{pattern:/(=\\s*)(?:(?!\\s)[-\\w\\xA0-\\uFFFF])+(?=\\s*$)/,lookbehind:!0}],operator:/[|~*^$]?=/}},\"n-th\":[{pattern:/(\\(\\s*)[+-]?\\d*[\\dn](?:\\s*[+-]\\s*\\d+)?(?=\\s*\\))/,lookbehind:!0,inside:{number:/[\\dn]+/,operator:/[+-]/}},{pattern:/(\\(\\s*)(?:even|odd)(?=\\s*\\))/i,lookbehind:!0}],combinator:/>|\\+|~|\\|\\|/,punctuation:/[(),]/}},e.languages.css.atrule.inside[\"selector-function-argument\"].inside=a,e.languages.insertBefore(\"css\",\"property\",{variable:{pattern:/(^|[^-\\w\\xA0-\\uFFFF])--(?!\\s)[-_a-z\\xA0-\\uFFFF](?:(?!\\s)[-\\w\\xA0-\\uFFFF])*/i,lookbehind:!0}});var r={pattern:/(\\b\\d+)(?:%|[a-z]+(?![\\w-]))/,lookbehind:!0},i={pattern:/(^|[^\\w.-])-?(?:\\d+(?:\\.\\d+)?|\\.\\d+)/,lookbehind:!0};e.languages.insertBefore(\"css\",\"function\",{operator:{pattern:/(\\s)[+\\-*\\/](?=\\s)/,lookbehind:!0},hexcode:{pattern:/\\B#[\\da-f]{3,8}\\b/i,alias:\"color\"},color:[{pattern:/(^|[^\\w-])(?:AliceBlue|AntiqueWhite|Aqua|Aquamarine|Azure|Beige|Bisque|Black|BlanchedAlmond|Blue|BlueViolet|Brown|BurlyWood|CadetBlue|Chartreuse|Chocolate|Coral|CornflowerBlue|Cornsilk|Crimson|Cyan|DarkBlue|DarkCyan|DarkGoldenRod|DarkGr[ae]y|DarkGreen|DarkKhaki|DarkMagenta|DarkOliveGreen|DarkOrange|DarkOrchid|DarkRed|DarkSalmon|DarkSeaGreen|DarkSlateBlue|DarkSlateGr[ae]y|DarkTurquoise|DarkViolet|DeepPink|DeepSkyBlue|DimGr[ae]y|DodgerBlue|FireBrick|FloralWhite|ForestGreen|Fuchsia|Gainsboro|GhostWhite|Gold|GoldenRod|Gr[ae]y|Green|GreenYellow|HoneyDew|HotPink|IndianRed|Indigo|Ivory|Khaki|Lavender|LavenderBlush|LawnGreen|LemonChiffon|LightBlue|LightCoral|LightCyan|LightGoldenRodYellow|LightGr[ae]y|LightGreen|LightPink|LightSalmon|LightSeaGreen|LightSkyBlue|LightSlateGr[ae]y|LightSteelBlue|LightYellow|Lime|LimeGreen|Linen|Magenta|Maroon|MediumAquaMarine|MediumBlue|MediumOrchid|MediumPurple|MediumSeaGreen|MediumSlateBlue|MediumSpringGreen|MediumTurquoise|MediumVioletRed|MidnightBlue|MintCream|MistyRose|Moccasin|NavajoWhite|Navy|OldLace|Olive|OliveDrab|Orange|OrangeRed|Orchid|PaleGoldenRod|PaleGreen|PaleTurquoise|PaleVioletRed|PapayaWhip|PeachPuff|Peru|Pink|Plum|PowderBlue|Purple|RebeccaPurple|Red|RosyBrown|RoyalBlue|SaddleBrown|Salmon|SandyBrown|SeaGreen|SeaShell|Sienna|Silver|SkyBlue|SlateBlue|SlateGr[ae]y|Snow|SpringGreen|SteelBlue|Tan|Teal|Thistle|Tomato|Transparent|Turquoise|Violet|Wheat|White|WhiteSmoke|Yellow|YellowGreen)(?![\\w-])/i,lookbehind:!0},{pattern:/\\b(?:hsl|rgb)\\(\\s*\\d{1,3}\\s*,\\s*\\d{1,3}%?\\s*,\\s*\\d{1,3}%?\\s*\\)\\B|\\b(?:hsl|rgb)a\\(\\s*\\d{1,3}\\s*,\\s*\\d{1,3}%?\\s*,\\s*\\d{1,3}%?\\s*,\\s*(?:0|0?\\.\\d+|1)\\s*\\)\\B/i,inside:{unit:r,number:i,function:/[\\w-]+(?=\\()/,punctuation:/[(),]/}}],entity:/\\\\[\\da-f]{1,8}/i,unit:r,number:i})}(Prism);\n" +
                "!function(e){var n=/\\b(?:abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|exports|extends|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|module|native|new|non-sealed|null|open|opens|package|permits|private|protected|provides|public|record(?!\\s*[(){}[\\]<>=%~.:,;?+\\-*/&|^])|requires|return|sealed|short|static|strictfp|super|switch|synchronized|this|throw|throws|to|transient|transitive|try|uses|var|void|volatile|while|with|yield)\\b/,t=\"(?:[a-z]\\\\w*\\\\s*\\\\.\\\\s*)*(?:[A-Z]\\\\w*\\\\s*\\\\.\\\\s*)*\",s={pattern:RegExp(\"(^|[^\\\\w.])\"+t+\"[A-Z](?:[\\\\d_A-Z]*[a-z]\\\\w*)?\\\\b\"),lookbehind:!0,inside:{namespace:{pattern:/^[a-z]\\w*(?:\\s*\\.\\s*[a-z]\\w*)*(?:\\s*\\.)?/,inside:{punctuation:/\\./}},punctuation:/\\./}};e.languages.java=e.languages.extend(\"clike\",{string:{pattern:/(^|[^\\\\])\"(?:\\\\.|[^\"\\\\\\r\\n])*\"/,lookbehind:!0,greedy:!0},\"class-name\":[s,{pattern:RegExp(\"(^|[^\\\\w.])\"+t+\"[A-Z]\\\\w*(?=\\\\s+\\\\w+\\\\s*[;,=()]|\\\\s*(?:\\\\[[\\\\s,]*\\\\]\\\\s*)?::\\\\s*new\\\\b)\"),lookbehind:!0,inside:s.inside},{pattern:RegExp(\"(\\\\b(?:class|enum|extends|implements|instanceof|interface|new|record|throws)\\\\s+)\"+t+\"[A-Z]\\\\w*\\\\b\"),lookbehind:!0,inside:s.inside}],keyword:n,function:[e.languages.clike.function,{pattern:/(::\\s*)[a-z_]\\w*/,lookbehind:!0}],number:/\\b0b[01][01_]*L?\\b|\\b0x(?:\\.[\\da-f_p+-]+|[\\da-f_]+(?:\\.[\\da-f_p+-]+)?)\\b|(?:\\b\\d[\\d_]*(?:\\.[\\d_]*)?|\\B\\.\\d[\\d_]*)(?:e[+-]?\\d[\\d_]*)?[dfl]?/i,operator:{pattern:/(^|[^.])(?:<<=?|>>>?=?|->|--|\\+\\+|&&|\\|\\||::|[?:~]|[-+*/%&|^!=<>]=?)/m,lookbehind:!0}}),e.languages.insertBefore(\"java\",\"string\",{\"triple-quoted-string\":{pattern:/\"\"\"[ \\t]*[\\r\\n](?:(?:\"|\"\")?(?:\\\\.|[^\"\\\\]))*\"\"\"/,greedy:!0,alias:\"string\"},char:{pattern:/'(?:\\\\.|[^'\\\\\\r\\n]){1,6}'/,greedy:!0}}),e.languages.insertBefore(\"java\",\"class-name\",{annotation:{pattern:/(^|[^.])@\\w+(?:\\s*\\.\\s*\\w+)*/,lookbehind:!0,alias:\"punctuation\"},generics:{pattern:/<(?:[\\w\\s,.?]|&(?!&)|<(?:[\\w\\s,.?]|&(?!&)|<(?:[\\w\\s,.?]|&(?!&)|<(?:[\\w\\s,.?]|&(?!&))*>)*>)*>)*>/,inside:{\"class-name\":s,keyword:n,punctuation:/[<>(),.:]/,operator:/[?&|]/}},import:[{pattern:RegExp(\"(\\\\bimport\\\\s+)\"+t+\"(?:[A-Z]\\\\w*|\\\\*)(?=\\\\s*;)\"),lookbehind:!0,inside:{namespace:s.inside.namespace,punctuation:/\\./,operator:/\\*/,\"class-name\":/\\w+/}},{pattern:RegExp(\"(\\\\bimport\\\\s+static\\\\s+)\"+t+\"(?:\\\\w+|\\\\*)(?=\\\\s*;)\"),lookbehind:!0,alias:\"static\",inside:{namespace:s.inside.namespace,static:/\\b\\w+$/,punctuation:/\\./,operator:/\\*/,\"class-name\":/\\w+/}}],namespace:{pattern:RegExp(\"(\\\\b(?:exports|import(?:\\\\s+static)?|module|open|opens|package|provides|requires|to|transitive|uses|with)\\\\s+)(?!<keyword>)[a-z]\\\\w*(?:\\\\.[a-z]\\\\w*)*\\\\.?\".replace(/<keyword>/g,(function(){return n.source}))),lookbehind:!0,inside:{punctuation:/\\./}}})}(Prism);\n" +
                "!function(){if(\"undefined\"!=typeof Prism&&\"undefined\"!=typeof document&&document.querySelector){var e,t=\"line-numbers\",i=\"linkable-line-numbers\",n=/\\n(?!$)/g,r=!0;Prism.plugins.lineHighlight={highlightLines:function(o,u,c){var h=(u=\"string\"==typeof u?u:o.getAttribute(\"data-line\")||\"\").replace(/\\s+/g,\"\").split(\",\").filter(Boolean),d=+o.getAttribute(\"data-line-offset\")||0,f=(function(){if(void 0===e){var t=document.createElement(\"div\");t.style.fontSize=\"13px\",t.style.lineHeight=\"1.5\",t.style.padding=\"0\",t.style.border=\"0\",t.innerHTML=\"&nbsp;<br />&nbsp;\",document.body.appendChild(t),e=38===t.offsetHeight,document.body.removeChild(t)}return e}()?parseInt:parseFloat)(getComputedStyle(o).lineHeight),p=Prism.util.isActive(o,t),g=o.querySelector(\"code\"),m=p?o:g||o,v=[],y=g.textContent.match(n),b=y?y.length+1:1,A=g&&m!=g?function(e,t){var i=getComputedStyle(e),n=getComputedStyle(t);function r(e){return+e.substr(0,e.length-2)}return t.offsetTop+r(n.borderTopWidth)+r(n.paddingTop)-r(i.paddingTop)}(o,g):0;h.forEach((function(e){var t=e.split(\"-\"),i=+t[0],n=+t[1]||i;if(!((n=Math.min(b,n))<i)){var r=o.querySelector('.line-highlight[data-range=\"'+e+'\"]')||document.createElement(\"div\");if(v.push((function(){r.setAttribute(\"aria-hidden\",\"true\"),r.setAttribute(\"data-range\",e),r.className=(c||\"\")+\" line-highlight\"})),p&&Prism.plugins.lineNumbers){var s=Prism.plugins.lineNumbers.getLine(o,i),l=Prism.plugins.lineNumbers.getLine(o,n);if(s){var a=s.offsetTop+A+\"px\";v.push((function(){r.style.top=a}))}if(l){var u=l.offsetTop-s.offsetTop+l.offsetHeight+\"px\";v.push((function(){r.style.height=u}))}}else v.push((function(){r.setAttribute(\"data-start\",String(i)),n>i&&r.setAttribute(\"data-end\",String(n)),r.style.top=(i-d-1)*f+A+\"px\",r.textContent=new Array(n-i+2).join(\" \\n\")}));v.push((function(){r.style.width=o.scrollWidth+\"px\"})),v.push((function(){m.appendChild(r)}))}}));var P=o.id;if(p&&Prism.util.isActive(o,i)&&P){l(o,i)||v.push((function(){o.classList.add(i)}));var E=parseInt(o.getAttribute(\"data-start\")||\"1\");s(\".line-numbers-rows > span\",o).forEach((function(e,t){var i=t+E;e.onclick=function(){var e=P+\".\"+i;r=!1,location.hash=e,setTimeout((function(){r=!0}),1)}}))}return function(){v.forEach(a)}}};var o=0;Prism.hooks.add(\"before-sanity-check\",(function(e){var t=e.element.parentElement;if(u(t)){var i=0;s(\".line-highlight\",t).forEach((function(e){i+=e.textContent.length,e.parentNode.removeChild(e)})),i&&/^(?: \\n)+$/.test(e.code.slice(-i))&&(e.code=e.code.slice(0,-i))}})),Prism.hooks.add(\"complete\",(function e(i){var n=i.element.parentElement;if(u(n)){clearTimeout(o);var r=Prism.plugins.lineNumbers,s=i.plugins&&i.plugins.lineNumbers;l(n,t)&&r&&!s?Prism.hooks.add(\"line-numbers\",e):(Prism.plugins.lineHighlight.highlightLines(n)(),o=setTimeout(c,1))}})),window.addEventListener(\"hashchange\",c),window.addEventListener(\"resize\",(function(){s(\"pre\").filter(u).map((function(e){return Prism.plugins.lineHighlight.highlightLines(e)})).forEach(a)}))}function s(e,t){return Array.prototype.slice.call((t||document).querySelectorAll(e))}function l(e,t){return e.classList.contains(t)}function a(e){e()}function u(e){return!!(e&&/pre/i.test(e.nodeName)&&(e.hasAttribute(\"data-line\")||e.id&&Prism.util.isActive(e,i)))}function c(){var e=location.hash.slice(1);s(\".temporary.line-highlight\").forEach((function(e){e.parentNode.removeChild(e)}));var t=(e.match(/\\.([\\d,-]+)$/)||[,\"\"])[1];if(t&&!document.getElementById(e)){var i=e.slice(0,e.lastIndexOf(\".\")),n=document.getElementById(i);n&&(n.hasAttribute(\"data-line\")||n.setAttribute(\"data-line\",\"\"),Prism.plugins.lineHighlight.highlightLines(n,t,\"temporary \")(),r&&document.querySelector(\".temporary.line-highlight\").scrollIntoView())}}}();\n" +
                "!function(){if(\"undefined\"!=typeof Prism&&\"undefined\"!=typeof document){var e=\"line-numbers\",n=/\\n(?!$)/g,t=Prism.plugins.lineNumbers={getLine:function(n,t){if(\"PRE\"===n.tagName&&n.classList.contains(e)){var i=n.querySelector(\".line-numbers-rows\");if(i){var r=parseInt(n.getAttribute(\"data-start\"),10)||1,s=r+(i.children.length-1);t<r&&(t=r),t>s&&(t=s);var l=t-r;return i.children[l]}}},resize:function(e){r([e])},assumeViewportIndependence:!0},i=void 0;window.addEventListener(\"resize\",(function(){t.assumeViewportIndependence&&i===window.innerWidth||(i=window.innerWidth,r(Array.prototype.slice.call(document.querySelectorAll(\"pre.line-numbers\"))))})),Prism.hooks.add(\"complete\",(function(t){if(t.code){var i=t.element,s=i.parentNode;if(s&&/pre/i.test(s.nodeName)&&!i.querySelector(\".line-numbers-rows\")&&Prism.util.isActive(i,e)){i.classList.remove(e),s.classList.add(e);var l,o=t.code.match(n),a=o?o.length+1:1,u=new Array(a+1).join(\"<span></span>\");(l=document.createElement(\"span\")).setAttribute(\"aria-hidden\",\"true\"),l.className=\"line-numbers-rows\",l.innerHTML=u,s.hasAttribute(\"data-start\")&&(s.style.counterReset=\"linenumber \"+(parseInt(s.getAttribute(\"data-start\"),10)-1)),t.element.appendChild(l),r([s]),Prism.hooks.run(\"line-numbers\",t)}}})),Prism.hooks.add(\"line-numbers\",(function(e){e.plugins=e.plugins||{},e.plugins.lineNumbers=!0}))}function r(e){if(0!=(e=e.filter((function(e){var n,t=(n=e,n?window.getComputedStyle?getComputedStyle(n):n.currentStyle||null:null)[\"white-space\"];return\"pre-wrap\"===t||\"pre-line\"===t}))).length){var t=e.map((function(e){var t=e.querySelector(\"code\"),i=e.querySelector(\".line-numbers-rows\");if(t&&i){var r=e.querySelector(\".line-numbers-sizer\"),s=t.textContent.split(n);r||((r=document.createElement(\"span\")).className=\"line-numbers-sizer\",t.appendChild(r)),r.innerHTML=\"0\",r.style.display=\"block\";var l=r.getBoundingClientRect().height;return r.innerHTML=\"\",{element:e,lines:s,lineHeights:[],oneLinerHeight:l,sizer:r}}})).filter(Boolean);t.forEach((function(e){var n=e.sizer,t=e.lines,i=e.lineHeights,r=e.oneLinerHeight;i[t.length-1]=void 0,t.forEach((function(e,t){if(e&&e.length>1){var s=n.appendChild(document.createElement(\"span\"));s.style.display=\"block\",s.textContent=e}else i[t]=r}))})),t.forEach((function(e){for(var n=e.sizer,t=e.lineHeights,i=0,r=0;r<t.length;r++)void 0===t[r]&&(t[r]=n.children[i++].getBoundingClientRect().height)})),t.forEach((function(e){var n=e.sizer,t=e.element.querySelector(\".line-numbers-rows\");n.style.display=\"none\",n.innerHTML=\"\",e.lineHeights.forEach((function(e,n){t.children[n].style.height=e+\"px\"}))}))}}}();\n" +
                "!function(){if(\"undefined\"!=typeof Prism&&\"undefined\"!=typeof document){var n=/<\\/?(?!\\d)[^\\s>\\/=$<%]+(?:\\s(?:\\s*[^\\s>\\/=]+(?:\\s*=\\s*(?:\"[^\"]*\"|'[^']*'|[^\\s'\">=]+(?=[\\s>]))|(?=[\\s/>])))+)?\\s*\\/?>/g,r=/^#?((?:[\\da-f]){3,4}|(?:[\\da-f]{2}){3,4})$/i,o=[function(n){var o=r.exec(n);if(o){for(var s=(n=o[1]).length>=6?2:1,e=n.length/s,t=1==s?1/15:1/255,i=[],a=0;a<e;a++){var c=parseInt(n.substr(a*s,s),16);i.push(c*t)}return 3==e&&i.push(1),\"rgba(\"+i.slice(0,3).map((function(n){return String(Math.round(255*n))})).join(\",\")+\",\"+String(Number(i[3].toFixed(3)))+\")\"}},function(n){var r=(new Option).style;return r.color=n,r.color?n:void 0}];Prism.hooks.add(\"wrap\",(function(r){if(\"color\"===r.type||r.classes.indexOf(\"color\")>=0){for(var s,e=r.content,t=e.split(n).join(\"\"),i=0,a=o.length;i<a&&!s;i++)s=o[i](t);if(!s)return;var c='<span class=\"inline-color-wrapper\"><span class=\"inline-color\" style=\"background-color:'+s+';\"></span></span>';r.content=c+e}}))}}();\n");
        try {
            File jsFile = new File(path + File.separator + "js");
            if (!jsFile.exists()) {
                jsFile.mkdirs();
            }
            File prismJS = new File(path + File.separator + "js" + File.separator + "prism.js");
            prismJS.createNewFile();
            PrintStream printStreamJS = new PrintStream(prismJS);
            printStreamJS.println(stringBuilder.toString());
            printStreamJS.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void genMyJS(String path,
                        String value1, String value2, String value3,
                        List<String> exceptionNameList, List<String> valueList){
        StringJoiner joiner1 = new StringJoiner("', '", "'", "'");
        for (String s : exceptionNameList) {
            joiner1.add(s);
        }
        StringBuilder joiner2 = new StringBuilder();
        for (int i = 0; i < exceptionNameList.size(); i++) {
            String s1 = valueList.get(i);
            String s2 = exceptionNameList.get(i);
            joiner2.append("{value:").append(s1).append(", name:'").append(s2).append("'},");
        }
        if (joiner2.length() > 0) {
            joiner2.deleteCharAt(joiner2.length() - 1);
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("option2 = {\n" +
                "    title: {\n" +
                "        text: '测试用例执行情况统计',\n" +
                "        left: 'center'\n" +
                "    },\n" +
                "    tooltip: {\n" +
                "        trigger: 'item'\n" +
                "    },\n" +
                "    legend: {\n" +
                "        orient: 'vertical',\n" +
                "        left: 'left'\n" +
                "    },\n" +
                "    series: [\n" +
                "        {\n" +
                "            name: 'Access From',\n" +
                "            type: 'pie',\n" +
                "            radius: '50%',\n" +
                "            data: [\n" +
                "                { value: ").
                append(value1).
                append(", name: '成功' },\n" +
                        "                { value:").
                append(value2).
                append(", name: '失败' },\n" +
                        "                { value:").
                append(value3).
                append(", name: '跳过' }\n" +
                        "            ],\n" +
                        "            emphasis: {\n" +
                        "                itemStyle: {\n" +
                        "                    shadowBlur: 10,\n" +
                        "                    shadowOffsetX: 0,\n" +
                        "                    shadowColor: 'rgba(0, 0, 0, 0.5)'\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "    ]\n" +
                        "};\n" +
                        "runPigID = document.getElementById('runPig');\n" +
                        "myChart = echarts.init(runPigID);\n" +
                        "myChart.setOption(option2);\n" +
                        "option1 = {\n" +
                        "    title: {\n" +
                        "        text: '测试用例触发的异常信息统计',\n" +
                        "        left: 'center'\n" +
                        "    },\n" +
                        "    tooltip: {\n" +
                        "        trigger: 'item'\n" +
                        "    },\n" +
                        "    legend: {\n" +
                        "        orient: 'vertical',\n" +
                        "        left: 'right',\n" +
                        "        data: [").
                append(joiner1).
                append("]\n" +
                        "    },\n" +
                        "    series: [\n" +
                        "        {\n" +
                        "            name: 'Access From',\n" +
                        "            type: 'pie',\n" +
                        "            radius: '50%',\n" +
                        "            center: ['25%', '50%'],\n" +
                        "            data:[").
                append(joiner2).
                append("],\n" +
                        "            emphasis: {\n" +
                        "                itemStyle: {\n" +
                        "                    shadowBlur: 10,\n" +
                        "                    shadowOffsetX: 0,\n" +
                        "                    shadowColor: 'rgba(0, 0, 0, 0.5)'\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "    ]\n" +
                        "};\n" +
                        "myChart1 = echarts.init(document.getElementById('exceptionPig'));\n" +
                        "myChart1.setOption(option1);");

        BufferedWriter writer = null;
        try {
            File jsFile = new File(path + File.separator + "js");
            if (!jsFile.exists()) {
                jsFile.mkdirs();
            }

            String filePath = path + File.separator + "js" + File.separator + "my.js";
            writer = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (filePath,false),"UTF-8"));
            writer.write(stringBuilder.toString());
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }

    /**
     * echarts.js is too big, move from other place
     * @param destPath
     */
    private static void genEchartJS(String destPath){
        try {
            File jsFile = new File(destPath + File.separator + "js"+ File.separator);
            if (!jsFile.exists()) {
                jsFile.mkdirs();
            }

            File echartsFile = new File(jsFile.getPath() + File.separator + "echarts.js");
            if (echartsFile.exists()) {
                return;
            } else {
                echartsFile.createNewFile();
            }

            InputStream inputStream = ReportFactory.class.getResourceAsStream("/echarts.js");
            FileUtils.copyInputStreamToFile(inputStream,echartsFile);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }

    private static void genIndexHTML(String path, String projectName,
                             Map<String, String> classInfo, String time,
                             List<String> methodNameList, List<String> classNameList, List<String> exceptionNameList){
        StringBuilder stringBuilder = new StringBuilder();
        // 统计信息
        stringBuilder.append("<!DOCTYPE html>\n" +
                "</html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width,initial-scale=1\" />\n" +
                "    <title>Justin Report</title>\n" +
                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"css/my.css\">\n" +
                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"css/prism.css\">\n" +
                "    <script src=\"js/echarts.js\"></script>\n" +
                "    <script src=\"js/prism.js\"></script>\n" +
                "</head>").
                append("<body>\n" +
                        "    <div class=\"topBar\">\n" +
                        "        <div class=\"helloDiv\">\n" +
                        "            <p> JustinStr 测试用例执行结果报告 !</p>").
                append("<p>").append(projectName).append("</p>").
                append("        </div>\n" +
                        "    </div>").
                append("    <div id=\"content\">\n" +
                        "        <div class=\"totalBar\" id=\"totalBar\">\n" +
                        "            <div class=\"miniTitle\">统计信息</div>\n" +
                        "            <div class=\"line\"></div>\n" +
                        "            <div class=\"totalDiv\">\n" +
                        "                <div class=\"totalDiv1\">\n" +
                        "                    <p class=\"fixTotalName\">测试类数目</p>").
                append("<div class=\"totalNum\" id=\"totalNum1\">").
                append(StringUtil.formatString(classInfo.get("class_num"))).
                append("</div>\n" +
                        "                </div>\n" +
                        "\n" +
                        "                <div class=\"totalDiv2\" style=\"background: #46dcbd\">\n" +
                        "                    <p class=\"fixTotalName\">待测方法总数</p>").
                append("<div class=\"totalNum\" id=\"totalNum2\">").
                append(StringUtil.formatString(classInfo.get("method_num"))).
                append("</div>\n" +
                        "                </div>\n" +
                        "\n" +
                        "                <div class=\"totalDiv2\" style=\"background: #ffc068\">\n" +
                        "                    <p class=\"fixTotalName\"> 被测试的方法数</p>").
                append("<div class=\"totalNum\" id=\"totalNum3\">").
                append(StringUtil.formatString(classInfo.get("test_method_num"))).
                append("</div>\n" +
                        "                </div>\n" +
                        "\n" +
                        "                <div class=\"totalDiv2\" style=\"background: #ff6e86\">\n" +
                        "                    <p class=\"fixTotalName\"> 测试用例个数</p>").
                append("<div class=\"totalNum\" id=\"totalNum4\">").
                append(StringUtil.formatString(classInfo.get("execute_case_num"))).
                append("</div>\n" +
                        "                </div>\n" +
                        "\n" +
                        "                <div class=\"totalDiv2\" style=\"background: rgb(226, 200, 255) \">\n" +
                        "                    <p class=\"fixTotalName\"> 执行时间</p>").
                append("<div class=\"totalNum\" id=\"totalNum5\">").
                append(time).
                append("</div>\n" +
                        "                </div>\n" +
                        "\n" +
                        "            </div>\n" +
                        "        </div>");


        // 数据视图
        stringBuilder.append("        <div class=\"pigBar\" id=\"pigBar\">\n" +
                "            <div class=\"miniTitle\">数据视图</div>\n" +
                "            <div class=\"line\"></div>\n" +
                "            <div class=\"chartsPig\">\n" +
                "                <div class=\"chartsPig1\" id=\"runPig\"> </div>\n" +
                "                <div class=\"chartsPig2\" id=\"exceptionPig\"></div>\n" +
                "            </div>\n" +
                "        </div>");

        // table
        stringBuilder.append("        <div class=\"detailBar\" id=\"detailBar\">\n" +
                "            <p class=\"miniTitle\">详细数据</p>\n" +
                "            <div class=\"line\"></div>\n" +
                "            <div class=\"tableDiv\">\n" +
                "                <div class=\"fixDetailDiv\">\n" +
                "                    <div class=\"detailMsgDiv\" id = \"detailMsgDiv\">\n" +
                "                        <p class=\"detailMsg\" id=\"detailTotalCount\">被测方法:").
                append(StringUtil.formatString(classInfo.get("test_method_num"))).
                append("</p>\n" +
                        "                        <div style=\"margin-left:6%; border-left:1px solid #CCC\"></div>\n" +
                        "                        <P class=\"detailMsg\" id=\"detailSuccessCount\"> 成功:").
                append(StringUtil.formatString(classInfo.get("success_count"))).
                append("</P>\n" +
                        "                        <div style=\"margin-left:6%; border-left:1px solid #CCC\"></div>\n" +
                        "                        <P class=\"detailMsg\" id=\"detailFailCount\"> 失败:").
                append(StringUtil.formatString(classInfo.get("failure_count"))).
                append("</P>\n" +
                        "                        <div style=\"margin-left:6%; border-left:1px solid #CCC\"></div>\n" +
                        "                        <P class=\"detailMsg\" id=\"detailSkipCount\"> 跳过:").
                append(StringUtil.formatString(classInfo.get("skip_count"))).
                append("</P>\n" +
                        "                    </div>\n" +
                        "                </div>\n" +
                        "                <table class=\"detailMsgTable\" id=\"detailMsgTable\">\n" +
                        "                    <thead class=\"tableHead\">\n" +
                        "                    <th class=\"tableTDA\">编号</th>\n" +
                        "                    <th>测试方法名称</th>\n" +
                        "                    <th>所在类名称</th>\n" +
                        "                    <th>异常名称</th>\n" +
                        "                    <th class=\"tableTDB\">堆栈信息</th>\n" +
                        "                    </thead>\n" +
                        "                    <tbody id=\"tbodyResult\">");

        StringBuilder tableInfo = new StringBuilder();
        for (int i = 0; i < exceptionNameList.size(); i++) {
            String methodName = methodNameList.get(i);
            tableInfo.append("<tr>\n" +
                    "                            <td class=\"tableTDA\">").
                    append(i+1).
                    append("</td>\n" +
                            "                            <td>").
                    append(methodNameList.get(i)).
                    append("</td>\n" +
                            "                            <td>").
                    append(classNameList.get(i)).
                    append("</td>\n" +
                            "                            <td>").
                    append(exceptionNameList.get(i)).
                    append("</td>\n" +
                            "                            <td class=\"tableTDB\">\n" +
                            "                                <a class=\"detailLi\" href=\"").
                    append("detail-html" + File.separator + classNameList.get(i).replaceAll("\\.",Matcher.quoteReplacement(File.separator)) + File.separator + methodName + ".html").
                    append("\">stack detail</a>\n" +
                            "                            </td>\n" +
                            "                        </tr>");
        }

        stringBuilder.append(tableInfo.toString()).
                append("</tbody>\n" +
                        "                </table>\n" +
                        "            </div>\n" +
                        "        </div>\n" +
                        "    </div>\n" +
                        "    <script src=\"js/my.js\"></script>\n" +
                        "\n" +
                        "</body>");

        BufferedWriter writer = null;
        try {
            String filePath = path + File.separator + "index.html";
            writer = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (filePath,false),"UTF-8"));
            writer.write(stringBuilder.toString());
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

    }

    public static void genDetailHTML(String projectName, String path, String className, String methodName, String testCaseInfo, String stackInfo){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <!--自适应屏幕    -->\n" +
                "    <meta name=\"viewport\" content=\"width=device-width,initial-scale=1\" />\n" +
                "    <title>JustinStr Report</title>\n" +
                "    <link rel=\"stylesheet\" type=\"text/css\" href=\"").
                append(path + File.separator + "css/my.css\">\n").
                append("    <link rel=\"stylesheet\" type=\"text/css\" href=\"").
                append(path + File.separator + "css/prism.css\">\n" +
                "\n" +
                "    <script src=\"https://code.jquery.com/jquery-3.5.1.min.js\"></script>\n" +
                "    <script src=\"").
                append(path + File.separator + "js/echarts.js\"></script>\n").
                append("    <script src=\"").
                append(path + File.separator + "js/my.js\"></script>\n").
                append("    <script src=\"").
                append(path + File.separator + "js/prism.js\"></script>\n" +
                "</head>\n" +
                "\n").
                append( "<body>\n" +
                "    <div class=\"topBar\">\n" +
                "        <div class=\"helloDiv\">\n" +
                "            <p> JustinStr 测试用例执行结果报告 !</p>\n").
                append("            <p>").
                append(projectName).
                append("</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "\n" +
                "    <div id=\"content\">\n" +
                "        <div class=\"detailStackBar\">\n" +
                "            <div class=\"miniTitle\">测试用例详细信息</div>\n" +
                "                <div class=\"line\"></div>\n" +
                "                <pre class=\"codePre\">\n" +
                "                    <code class=\"language-java\" >\n").
                append(testCaseInfo).
                append("</code>\n" +
                        "                </pre>\n" +
                        "            </div>\n" +
                        "\n" +
                        "        <div class=\"detailStackBar\">\n" +
                        "            <div class=\"miniTitle\">堆栈详细信息</div>\n" +
                        "            <div class=\"line\"></div>\n" +
                        "            <pre class=\"codePre\">\n" +
                        "                <code class=\"language-java\" >\n").
                append(stackInfo).
                append("</code>\n" +
                        "            </pre>\n" +
                        "        <div>\n" +
                        "\n" +
                        "    </div>\n" +
                        "    </div>\n" +
                        "    </div>\n" +
                        "</body>");

        BufferedWriter writer = null;
        try {
            File detailFolder = new File(path + File.separator + "detail-html" + File.separator + className.replaceAll("\\.", Matcher.quoteReplacement(File.separator)));

            if (!detailFolder.exists()){
                detailFolder.mkdirs();
            }
            String filePath = detailFolder +  File.separator + methodName +  ".html";
            writer = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (filePath,false),"UTF-8"));
            writer.write(stringBuilder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}
