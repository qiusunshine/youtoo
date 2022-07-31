(function(){
    var arr = document.querySelectorAll('a[copyhref]');
    for(var i = 0; i < arr.length; i++){
        var tag = arr[i];
        if(tag != null){
            var aHref = tag.getAttribute('copyhref');
            tag.removeAttribute('copyhref');
            tag.setAttribute('href',aHref);
        }
    }
})();