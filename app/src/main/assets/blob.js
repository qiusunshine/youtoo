(function(){
try{
var request = new XMLHttpRequest();
let uuu = '${uuu}';
let fileName = '${fileName}';
request.open('GET', uuu, true);
request.setRequestHeader('Content-type', 'text/plain');
request.responseType = 'blob';
request.onload = function (e) {
   try {
       var headers = request.getAllResponseHeaders();
       var arr = headers.trim().split(/[\r\n]+/);
       var headerMap = {};
       arr.forEach(function (line) {
           var parts = line.split(': ');
           var header = parts.shift();
           var value = parts.join(': ');
           headerMap[header] = value;
       });
       console.log('downloadBlob2:' + this.status);
       if (this.status === 200) {
            fy_bridge_app.downloadBlobUpdate(uuu, '0/100');
            var blobFile = this.response;
            var reader = new FileReader();
            reader.readAsDataURL(blobFile);
            reader.onloadend = function() {
               var base64data = reader.result;
               fy_bridge_app.downloadBlob(uuu, fileName, JSON.stringify(headerMap), base64data);
            }
            reader.onprogress = function (event) {
               let aa = Math.round(event.loaded / event.total * 100);
               fy_bridge_app.downloadBlobUpdate(uuu, aa + '/100');
            }
        }
    }catch(e){
        fy_bridge_app.log('blobDownloadError4:' + e.toString() + "==>" + JSON.stringify(e));
        console.log(e);
    }
}
request.addEventListener('error', function(e){
    fy_bridge_app.log('blobDownloadError1:' + e.toString() + "==>" + JSON.stringify(e));
    console.log(e);
});
request.addEventListener('abort', function(e){
  fy_bridge_app.log('blobDownloadError3:' + e.toString() + "==>" + JSON.stringify(e));
  console.log(e);
});
request.send();
}catch(e){
    fy_bridge_app.log('blobDownloadError2:' + e.toString() + "==>" + JSON.stringify(e));
    console.log(e);
}
})()