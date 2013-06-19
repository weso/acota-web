			function Form() {
			    var modified = false;

			    this.result = $(".result");
			    this.feedback = $(".feedback");

			    this.rank = new Array();
			    this.tags = new Array();

			    this.uri = " ";
			    this.label = " ";
			    this.description = " ";

			    this.serial = 0;

			    this.ws = $("div[data-ws]").attr("data-ws");
			};

			Form.prototype.isReady = function () {
			    return this.label.length > 15 && this.description.length > 30;
			}

			Form.prototype.getJson = function () {
			    return {
			    	"id": this.id,
			        "uri": this.uri,
			        "label": this.label,
			        "description": this.description		
			    };
			}

			var form = new Form();

			var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
			var wsSocket = new WS(form.ws);

			$('#uri').change(function () {
			    form.uri = $(this).val();
			    doPost();
			});

			$('#uri').keyup(function (evt) {
			    form.uri = $(this).val();
			    if (evt.keyCode == 32) {
			        doPost();
			    }
			});

			$('#label').change(function () {
			    form.label = $(this).val();
			    doPost();
			});

			$('#label').keyup(function (evt) {
			    form.label = $(this).val();
			    if (evt.keyCode == 32) {
			        doPost();
			    }
			});

			$('#description').change(function () {
			    form.description = $(this).val();
			    doPost();
			});

			$('#description').keyup(function (evt) {
			    form.description = $(this).val();
			    if (evt.keyCode == 32) {
			        doPost();
			    }
			});
			
			$('#addCustomTag').click(function(){
				var text = $('#customTag').val();
				$('#customTag').val("")
				addTag(text);
			});
			
			$('form#custom').submit(function (e) {
				e.preventDefault();
				var text = $('#customTag').val();
				$('#customTag').val("")
				addTag(text);
			});

			var receiveEvent = function (event) {
			    var data = JSON.parse(event.data);
			    var i = 1;
			    if(data.id!=null || data.id!=undefined){
			    	form.id = data.id;
			    }else{
					$.each(data.recommendations, function () {
						var current = form.rank[i]
						if (current == null || current == undefined) {
						    form.result.append('<div class="pillb"><span class="rank">' + (i) + '</span><span class="tag" data-rank="' + (i) + '">' + this.label + '</span><span class="lang">' + this.lang + '</span></div>');
						    form.rank[i] = $('span[data-rank=' + i + ']');
						    form.rank[i].click(function(){
						    	addTag($(this).text());
						    });
						} else {
						    form.rank[i].text(this.label);
						}
						i++;
					});
				}
			}

			function addTag(text) {
				if(form.tags[text]==null){
					form.tags[text]=text;
					form.feedback.append('<div class="pilla"><span class="tag">' + text + '</span><span class="rem" data-tag="' + text + '"> x </span></div>').fadeIn(300);
					$('span[data-tag="' + text + '"]').click(function(){
						var text = $(this).data('tag');
						delete form.tags[text];
						$(this).parent().fadeOut(function(){$(this).remove()});
					});
				}
			}

			function doPost() {

			   if (form.isReady()) {
	           	wsSocket.send(JSON.stringify(form.getJson()));		            
			   }
			};

			wsSocket.onmessage = receiveEvent;
			
			wsSocket.onopen = function(){
				wsSocket.send(JSON.stringify({"id":"?"}));
			}
