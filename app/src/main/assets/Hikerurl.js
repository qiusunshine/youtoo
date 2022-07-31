/**
 * @name 海阔url点击事件生成工具
 * @Author LoyDgIk
 * @version 4
 */
;
(function(windows) {
    "use strict";
    //var [emptyUrl, lazyRule, rule, x5, input, confirm] = ["hiker://empty",  "@rule=", "x5WebView://", "input://", "confirm://"];
    var head = {
        empty: "hiker://empty",
        lazyRule: "@lazyRule=",
        rule: "@rule=",
        x5: "x5WebView://",
        input: "input://",
        confirm: "confirm://",
        x5Lazy: "x5Rule://",
        select: "select://",
        webLazy: "webRule://",
    }

    function HikerUrl(param1, param2, param3) {
        this.param1 = param1 === "" ? "" : (param1 || head.empty);
        this.param2 = param2 || "";
        this.param3 = param3;
        this.isbase64 = false;
        this.url = this.param1;
        this.ruleOrdinary = this.param2;
    }

    function $(param1, param2, param3) {
        return new HikerUrl(param1, param2, param3);
    }
    //静态方法
    Object.assign($, {
        hiker: windows,
        exports: {},
        toString() {
            if (arguments.length === 0) {
                return "$";
            } else {
                return toStringFun(arguments);
            }
        },
        require(path, headers, time) {
            path = path || "";
            let req_code = "";
            let have = loadedUrl.find(item => item.path === path);
            if (have) {
                return have.value;
            }
            if (path.startsWith("hiker://page/")) {
                req_code = JSON.parse(request(path) || '{"rule":""}').rule;
            } else if (path.startsWith("hiker://files/") || path.startsWith("file://")) {
                req_code = request(path);
            } else if (path.startsWith("http://") || path.startsWith("https://")) {
                let pathlibs = "hiker://files/libs/" + md5(path) + ".js";
                if ($.type(time) == "number") {
                    fetchCache(path, time, headers);
                    req_code = request(pathlibs);
                } else {
                    if (fileExist(pathlibs)) {
                        req_code = request(pathlibs);
                    } else {
                        req_code = request(path, headers);
                        writeFile(pathlibs, req_code);
                    }
                }
            } else {
                return {};
            }
            let temexports = $.exports;
            new Function(req_code).apply(windows);
            let value = $.exports;
            loadedUrl.push({
                path: path,
                value: value
            });
            $.exports = temexports;
            return value;
        },
        type(obj) {
            if (obj == null) {
                return String(obj);
            }
            return typeof obj === "object" || typeof obj === "function" ?
                class2type[core_toString.call(obj)] || "object" :
                typeof obj;
        },
        dateFormat(date, text) {
            if ($.type(date) !== "date" && $.type(date) !== "number") {
                throw new Error("Cannot format given Object as a Date");
            }
            if ($.type(text) !== "string") {
                throw new Error("Text should be String");
            }

            let simpleDateFormat;
            if (dateFormatCache.text === text) {
                simpleDateFormat = dateFormatCache.value;
            } else {
                simpleDateFormat = new java.text.SimpleDateFormat(text);
                dateFormatCache.text = text;
                dateFormatCache.value = simpleDateFormat;
            }
            return String(simpleDateFormat.format(date));
        },
        stringify(Data, Pattern) {
            switch (Object.prototype.toString.call(Data)) {
                case "[object Undefined]":
                    return "undefined";
                    break;
                case "[object Null]":
                    return "null";
                    break;
                case "[object Function]":
                    return Data.toString();
                    break;
                case "[object Array]":
                    return "[" + Data.map(item => {
                        return $.stringify(item);
                    }).toString() + "]";
                    break;
                case "[object Object]":
                    return "{" + Object.keys(Data).map(item => {
                        return '"' + item + '":' + $.stringify(Data[item]);
                    }).join(",") + "}";
                    break;
                default:
                    return JSON.stringify(Data);
            }
        },
        log(obj) {
            for (let i = 0; i < arguments.length; i++) {
                log(arguments[i]);
            }
            return obj;
        }

    });
    let dateFormatCache = {};
    let class2type = {},
        loadedUrl = [],
        classtype = ["Boolean", "Number", "String,", "Function", "Array", "Date", "RegExp", "Object", "Error", "Symbol"],
        core_toString = class2type.toString;

    classtype.forEach((name) => {
        class2type["[object " + name + "]"] = name.toLowerCase();
    });

    function base64Func(tg, funcStr) {
        if (tg.isbase64) {
            let q = tg.base64quote;
            return 'eval(base64Decode(' + q + base64Encode(funcStr) + q + '));';
        } else {
            return funcStr;
        }
    }

    function getFileTime(path) {
        let file = new java.io.File(path);
        let lastModified = file.lastModified();
        let date = new Date(lastModified);
        return date.getTime();

    }

    function toStringFun(arr) {
        var args = [];
        for (let i = 1, j = 0; i < arr.length; i++, j++) {
            args[j] = $.stringify(arr[i]);
        }
        if (typeof arr[0] === "function") {
            return "(" + arr[0] + ")(" + args.toString() + ")";
        } else {
            return "";
        }
    }
    //方法
    $.fn = {
        constructor: HikerUrl,
        b64(quote) {
            this.base64quote = quote || "\"";
            this.isbase64 = !this.isbase64;
            return this;
        },
        rule() {
            return this.param1 + head.rule + (this.param2 || "js:" + base64Func(this, toStringFun(arguments)));
        },
        lazyRule() {
            return this.param1 + head.lazyRule + this.param2 + ".js:" + base64Func(this, toStringFun(arguments));
        },
        x5Rule() {
            return (this.param1 == "" ? "" : head.x5) + "javascript:var input=" + JSON.stringify(this.param1) + ";" + toStringFun(arguments);
        },
        input() {
            return head.input + JSON.stringify({
                value: this.param1,
                hint: this.param2,
                js: toStringFun(arguments)
            });
        },
        confirm() {
            return head.confirm + this.param1 + ".js:" + base64Func(this, toStringFun(arguments));
        },
        x5Lazy() {
            return head.x5Lazy + this.param1 + "@" + toStringFun(arguments);
        },
        webLazy() {
            return head.webLazy + this.param1 + "@" + toStringFun(arguments);
        },
        select() {
            return head.select + JSON.stringify({
                title: this.param3,
                options: Array.isArray(this.param1) ? this.param1 : [],
                col: this.param2 || 1,
                js: toStringFun(arguments)
            });
        }
    }
    HikerUrl.prototype = $.prototype = $.fn;
    //if(typeof (windows.$) === 'undefined') {
    //对外接口
    windows.$ = Object.seal($);
    // }
})(this);
(function() {
    const data = Symbol("data");
    const build = Symbol("build");
    const forbid = Symbol("forbid");
    let _$_ = $;
    let _MY_TYPE_ = typeof MY_TYPE === "undefined" ? "eval" : MY_TYPE;

    function HikerUrl(param) {
        if (!Array.isArray(param)) {
            throw new Error("HikerUrl[U]:非法参数");
        }
        this.param = param;
        this[data] = [];
    }

    function HikerUrlData(input, paramArr, skip) {
        if (!(Array.isArray(input) || input === undefined)) {
            throw new Error("HikerUrlData:非法参数");
        }
        this.input = input || [];
        this.paramArr = paramArr;
        this.skip = skip || 0;
    }

    function then(input, paramArr, skip) {
        return new HikerUrlData(input, paramArr, skip);
    }

    function $U() {
        return new HikerUrl(Array.from(arguments) || []);
    }
    const HIKERSET = ["lazyRule", "rule", "input", "confirm", "select", "x5Lazy", "webLazy"];
    HIKERSET.forEach((key) => {
        HikerUrl.prototype[key] = function(fun) {
            if (this[forbid]) {
                throw new Error("HikerUrl[U]:rule后不能继续调用");
            } else if (key === "rule") {
                this[forbid] = true;
            }
            this[data].push([key, fun, [].slice.call(arguments, 1)]);
            return this;
        }
    });
    HikerUrl.prototype[build] = function(param) {
        let funList = this[data];
        if (!(Array.isArray(funList) && funList.length)) {
            throw new Error("HikerUrl[U]:函数调用链不存在");
        }
        let item = funList.shift();
        let $tem = _$_.apply(_$_, this.param);
        if (item[0] == "lazyRule" || item[0] == "rule") {
            $tem.b64("'");
        }
        return $tem[item[0]]((targetFunItem, funList, param) => {
            return $U.runCode(targetFunItem, funList, param);
        }, item, funList, param);
    };
    Object.assign(HikerUrl.prototype, {
        init(inputArr, funList) {
            this.param = inputArr;
            this[data] = funList || [];
            return this;
        }
    });
    Object.assign(HikerUrl, {
        runCode(targetFunItem, funList, param) {
            let [type, fun, paramArr] = targetFunItem;
            _$_.hiker.SUPER = param;
            if (funList.length === 0) {
                _$_.hiker.then = undefined;
            }
            let paramObject = fun.apply(fun, paramArr);
            if (funList.length && paramObject instanceof HikerUrlData) {
                funList = paramObject.skip ? funList.slice(paramObject.skip) : funList;
                let $hikerObject = $U().init(paramObject.input, funList);
                return $hikerObject[build](paramObject.paramArr);
            } else {
                return paramObject;
            }
        }
    });

    function buildHikerUrl(data) {
        if (data instanceof HikerUrl) {
            return data[build]();
        } else if (Array.isArray(data)) {
            let layout = [];
            for (let i = 0; i < data.length; i++) {
                let it = data[i];
                if (it.url instanceof HikerUrl) {
                    it.url = it.url[build]();
                }
                layout.push(it);
            }
            return layout;
        }
    }
    $U.runCode = (a, b, c) => HikerUrl.runCode(a, b, c);
    $U.build = (data) => buildHikerUrl(data);
    _$_.hiker.$U = $U;
    if (_MY_TYPE_ === "eval") {
        _$_.hiker.then = then;
    }
})()