(function(){
    var arr = document.querySelectorAll('a[href]');
    for(var i = 0; i < arr.length; i++){
        var tag = arr[i];
        if(tag != null){
            var aHref = tag.getAttribute('href');
            tag.removeAttribute('href');
            tag.setAttribute('copyhref',aHref);
        }
    }
})();