(function () {
    let target = {playbackRate};
    let arr = document.querySelectorAll('video');
    if(arr && arr.length > 0)
    for (let i = 0; i < arr.length; i++) {
        let tag = arr[i];
        if (tag != null) {
            tag.playbackRate = target;
        }
    }

    let iframe = document.querySelectorAll('iframe');
    if (iframe && iframe.length > 0) {
        for (let i = 0; i < iframe.length; i++) {
            try {
                arr = iframe[i].contentWindow.document.querySelectorAll('video')
                if(arr && arr.length > 0)
                for (let j = 0; j < arr.length; j++) {
                    let tag = arr[j];
                    if (tag != null) {
                        tag.playbackRate = target;
                    }
                }
            } catch(e){
                console.log(e)
            }
        }
    }
})();