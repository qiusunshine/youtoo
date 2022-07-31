/**
 * JS环境增强
 */
if (MY_RULE && typeof _displayName != 'undefined') {
//    log("resetTitle:" + MY_RULE.title + "->" + _displayName)
    MY_RULE._title = MY_RULE.title;
    MY_RULE.title = _displayName || MY_RULE.title;
}
;
(function(windows) {
    windows.MY_NAME = '嗅觉浏览器';
    function initConfig(obj){
        let config = {}
        if(obj != null && "{}" != JSON.stringify(obj)){
            let _cfg = getItem('initConfig', '{}');
            if(_cfg && _cfg.length > 0){
                config = JSON.parse(_cfg)
            }
            for(let key of Object.keys(obj)){
                config[key] = obj[key]
            }
        }
        setItem('initConfig', JSON.stringify(config));
        windows.config = config;
    }
    windows.initConfig = initConfig;
    try {
        Object.defineProperty(windows, "config", {
            enumerable: true,
            configurable: true,
            get: function () {
                //懒加载
                if(windows.config0 == null){
                    //log('config0 == null');
                    let _cfg = getItem('initConfig', '{}');
                    let config = {};
                    if(_cfg && _cfg.length > 0){
                        config = JSON.parse(_cfg);
                    }
                    windows.config0 = config;
                    return config;
                } else {
                    //log('config0 != null');
                    return windows.config0;
                }
            },
            set: function(v) {
                windows.config0 = v;
            }
        });
    } catch(e){}

    windows.console = {
        log: log,
        info: log,
        error: log,
        debug: log,
        warn: log,
        clear: function(){
        }
    };

    /**
    * http接口
    */
    var _fetch0 = fetch;
    var _post0 = post;
    var _request0 = request;
    var _postRequest0 = postRequest;
    var _fetchCookie0 = fetchCookie;

    function _httpFetch(url, options){
        this.url = url;
        this.options = options || {};
        this._error = () => {};
        this._success = () => {};
        this._func = _fetch0;
    }

    function startFetch(that, func){
        try {
            let _a = func(that.url, that.options) || "";
            if(_a == "" || _a.startsWith('error:')) {
                that._error(_a.replace("error:", ""));
            } else if(_a.startsWith("{") && _a.endsWith("}")){
                try {
                    let json = JSON.parse(_a);
                    if(json.url && json.headers){
                        if(json.error){
                            that._error(json.error);
                        } else {
                            if(json.body && json.body.startsWith("{") && json.body.endsWith("}")){
                                try {
                                    json.body = JSON.parse(json.body);
                                } catch(e) {
                                }
                            }
                            that._success(json);
                        }
                    } else {
                        that._success(json);
                    }
                } catch(e) {
                    that._success(_a);
                }
            } else {
                that._success(_a);
            }
        } catch(e) {
            that._error(e);
        }
    }

    _httpFetch.fn = {
        success: function(func){
            this._success = func;
            return this;
        },
        error: function(func){
            this._error = func;
            return this;
        },
        func: function(ff){
            this._func = ff;
            return this;
        },
        headers: function(hds){
            if(!this.options){
                this.options = {};
            }
            Object.assign(this.options, {headers: hds});
            return this;
        },
        start: function() {
            startFetch(this, this._func || _fetch0)
            return this;
        }
    }

    let func0 = {
        fetch: (url, options) => {
            return new _httpFetch(url, options);
        },
        post: (url, options) => {
            return new _httpFetch(url, options).func(_post0);
        },
        request: (url, options) => {
         return new _httpFetch(url, options).func(_request0);
        },
        postRequest: (url, options) => {
            return new _httpFetch(url, options).func(_postRequest0);
        },
        fetchCookie: (url, options) => {
            return new _httpFetch(url, options).func(_fetchCookie0);
        }
    }

    Object.assign(_httpFetch.fn, func0);
    _httpFetch.prototype = _httpFetch.fn;

    windows.http = Object.assign({}, func0);

    /*
    * atob
    */
    windows.window0 = {
      btoa: function(s) {
          let base64hash0 = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';
          if (/([^\u0000-\u00ff])/.test(s)) {
              throw new Error('INVALID_CHARACTER_ERR');
          }
          let i = 0,
              prev,
              ascii,
              mod,
              result = [];
          while (i < s.length) {
              ascii = s.charCodeAt(i);
              mod = i % 3;
              switch (mod) {
                  case 0:
                      result.push(base64hash0.charAt(ascii >> 2));
                      break;
                  case 1:
                      result.push(base64hash0.charAt((prev & 3) << 4 | (ascii >> 4)));
                      break;
                  case 2:
                      result.push(base64hash0.charAt((prev & 0x0f) << 2 | (ascii >> 6)));
                      result.push(base64hash0.charAt(ascii & 0x3f));
                      break;
              }
              prev = ascii;
              i++;
          }
          if (mod == 0) {
              result.push(base64hash0.charAt((prev & 3) << 4));
              result.push('==');
          } else if (mod == 1) {
              result.push(base64hash0.charAt((prev & 0x0f) << 2));
              result.push('=');
          }
          return result.join('');
      },
      atob: function(s) {
          let base64hash0 = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';
          s = s.replace(/\s|=/g, '');
          let cur,
              prev,
              mod,
              i = 0,
              result = [];
          while (i < s.length) {
              cur = base64hash0.indexOf(s.charAt(i));
              mod = i % 4;
              switch (mod) {
                  case 0:
                      //TODO
                      break;
                  case 1:
                      result.push(String.fromCharCode(prev << 2 | cur >> 4));
                      break;
                  case 2:
                      result.push(String.fromCharCode((prev & 0x0f) << 4 | cur >> 2));
                      break;
                  case 3:
                      result.push(String.fromCharCode((prev & 3) << 6 | cur));
                      break;
              }
              prev = cur;
              i++;
          }
          return result.join('');
      }
    }

    //图片转base64
    function convertBase64Image(url) {
        const File = java.io.File;
        let javaImport = new JavaImporter();
        javaImport.importPackage(
            Packages.com.example.hikerview.utils
        );
        with(javaImport) {
            let png = "hiker://files/cache/test.png";
            downloadFile(url, png);
            let path = getPath(png).replace("file://", "");
            let bs = _base64.encodeToString(FileUtil.fileToBytes(path), _base64.NO_WRAP);
            new File(path).delete();
            return "data:image/jpeg;base64," + bs;
        }
        return "";
    }
    windows.convertBase64Image = convertBase64Image;

    windows.findItem = function(a){
        let b = windows._findItem(a);
        try {
            if(b && b.extra){
                b.extra = JSON.parse(b.extra);
            }
        } catch(e) {}
        return b;
    }

    windows.findItemsByCls = function(a){
        let b = windows._findItemsByCls(a);
        try {
            if(b){
                for(let i in b){
                    if(b[i] && b[i].extra && b[i].extra.length > 0){
                        b[i].extra = JSON.parse(b[i].extra);
                    }
                }
            }
        } catch(e) {}
        return b;
    }

    function toJsonObj(a) {
        if (!a) {
            return a;
        }
        if(a.startsWith('{') && a.endsWith('}')){
            try {
                return JSON.parse(a);
            }catch(e){}
        }
        if(a.startsWith('[') && a.endsWith(']')){
            try {
                return JSON.parse(a);
            }catch(e){}
        }
        return a;
    }

    function toJsonStr(obj){
        if (obj == null) {
            obj = '';
        }
        if ($.type(obj) !== "string") {
            obj = JSON.stringify(obj);
        }
        return obj;
    }

    windows.storage0 = {
        getItem: function(key, defaultVal) {
            let a = windows.getItem(key, toJsonStr(defaultVal));
            let json = toJsonObj(a);
            return json;
        },
        setItem: function(key, obj){
            obj = toJsonStr(obj);
            windows.setItem(key, obj);
        },
        getVar: function(key, defaultVal) {
            let a = windows.getVar(key, toJsonStr(defaultVal));
            let json = toJsonObj(a);
            return json;
        },
        putVar: function(key, obj){
            obj = toJsonStr(obj);
            windows.putVar(key, obj);
        },
        getMyVar: function(key, defaultVal) {
            let a = windows.getMyVar(key, toJsonStr(defaultVal));
            let json = toJsonObj(a);
            return json;
        },
        putMyVar: function(key, obj) {
            obj = toJsonStr(obj);
            windows.putMyVar(key, obj);
        }
    }

})(this);