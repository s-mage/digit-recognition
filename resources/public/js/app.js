function resize() {
    var canvas = document.getElementById("canvas-small");
    var canvas_big = document.getElementById("canvas-big");
    var ctx = canvas.getContext("2d");
    var img = new Image();
    img.crossOrigin = "Anonymous"; //cors support
    img.onload = function(){
        var W = img.width;
        var H = img.height;
        canvas.width = W;
        canvas.height = H;
        ctx.drawImage(img, 0, 0); //draw image

        //resize manually
        resample_hermite(canvas, W, H, 28, 28);
    }
    img.src = canvas_big.toDataURL();
    return true;
};

var canvas_big = document.getElementById("canvas-big");
var context = canvas_big.getContext("2d");

function getFeatureVector() {
    var canvas_small = document.getElementById("canvas-small");
    var context_small = canvas_small.getContext("2d");
    var data = context_small.getImageData(0, 0, 28, 28).data;
    var result = [];
    for(var i = 0; i < data.length; i += 4) {
        result.push(data[i + 3] / 255);
    };
    return result;
};

$('#clear').click(function(e) {
    var canvas_small = document.getElementById("canvas-small");
    var context_small = canvas_small.getContext("2d");
    context_small.clearRect(0, 0, canvas_small.width, canvas_small.height);
    context.clearRect(0, 0, canvas_big.width, canvas_big.height);
    clickX = new Array();
    clickY = new Array();
});

$('#send').click(function(e) {
    $.post("/recognize", { features: getFeatureVector() })
        .done(function(data) { alert(data); });
});

$('#canvas-big').mousedown(function(e){
  var mouseX = e.pageX - this.offsetLeft;
  var mouseY = e.pageY - this.offsetTop;
		
  paint = true;
  addClick(e.pageX - this.offsetLeft, e.pageY - this.offsetTop);
  redraw();
});

$('#canvas-big').mousemove(function(e){
  if(paint){
    addClick(e.pageX - this.offsetLeft, e.pageY - this.offsetTop, true);
    redraw();
  }
});

$('#canvas-big').mouseup(function(e){
  paint = false;
  resize();
});

$('#canvas').mouseleave(function(e){
  paint = false;
  resize();
});

var clickX = new Array();
var clickY = new Array();
var clickDrag = new Array();
var paint;

function addClick(x, y, dragging)
{
  clickX.push(x);
  clickY.push(y);
  clickDrag.push(dragging);
}

function redraw(){
  context.clearRect(0, 0, context.canvas.width, context.canvas.height); // Clears the canvas
  
  context.strokeStyle = "#000000";
  context.lineJoin = "round";
  context.lineWidth = 10;
			
  for(var i=0; i < clickX.length; i++) {		
    context.beginPath();
    if(clickDrag[i] && i){
      context.moveTo(clickX[i-1], clickY[i-1]);
     }else{
       context.moveTo(clickX[i]-1, clickY[i]);
     }
     context.lineTo(clickX[i], clickY[i]);
     context.closePath();
     context.stroke();
  }
}

function resample_hermite(canvas, W, H, W2, H2){
    var time1 = Date.now();
    W2 = Math.round(W2);
    H2 = Math.round(H2);
    var img = canvas.getContext("2d").getImageData(0, 0, W, H);
    var img2 = canvas.getContext("2d").getImageData(0, 0, W2, H2);
    var data = img.data;
    var data2 = img2.data;
    var ratio_w = W / W2;
    var ratio_h = H / H2;
    var ratio_w_half = Math.ceil(ratio_w/2);
    var ratio_h_half = Math.ceil(ratio_h/2);

    for(var j = 0; j < H2; j++){
        for(var i = 0; i < W2; i++){
            var x2 = (i + j*W2) * 4;
            var weight = 0;
            var weights = 0;
            var weights_alpha = 0;
            var gx_r = gx_g = gx_b = gx_a = 0;
            var center_y = (j + 0.5) * ratio_h;
            for(var yy = Math.floor(j * ratio_h); yy < (j + 1) * ratio_h; yy++){
                var dy = Math.abs(center_y - (yy + 0.5)) / ratio_h_half;
                var center_x = (i + 0.5) * ratio_w;
                var w0 = dy*dy //pre-calc part of w
                    for(var xx = Math.floor(i * ratio_w); xx < (i + 1) * ratio_w; xx++){
                        var dx = Math.abs(center_x - (xx + 0.5)) / ratio_w_half;
                        var w = Math.sqrt(w0 + dx*dx);
                        if(w >= -1 && w <= 1){
                            //hermite filter
                            weight = 2 * w*w*w - 3*w*w + 1;
                            if(weight > 0){
                                dx = 4*(xx + yy*W);
                                //alpha
                                gx_a += weight * data[dx + 3];
                                weights_alpha += weight;
                                //colors
                                if(data[dx + 3] < 255)
                                    weight = weight * data[dx + 3] / 250;
                                gx_r += weight * data[dx];
                                gx_g += weight * data[dx + 1];
                                gx_b += weight * data[dx + 2];
                                weights += weight;
                            }
                        }
                    }		
            }
            data2[x2]     = gx_r / weights;
            data2[x2 + 1] = gx_g / weights;
            data2[x2 + 2] = gx_b / weights;
            data2[x2 + 3] = gx_a / weights_alpha;
        }
    }
    console.log("hermite = "+(Math.round(Date.now() - time1)/1000)+" s");
    canvas.getContext("2d").clearRect(0, 0, Math.max(W, W2), Math.max(H, H2));
    canvas.width = W2;
    canvas.height = H2;
    canvas.getContext("2d").putImageData(img2, 0, 0);
}
