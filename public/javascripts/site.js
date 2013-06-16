			function Form(){
				var modified = false;
				
				this.result = $(".result");
				
				this.rank = new Array();
				
				this.uri = " ";
				this.label = " ";
				this.description = " ";
			};
			
			Form.prototype.isReady = function(){
				return this.label.length > 15 && this.description.length > 30;
			}
			
			Form.prototype.getJson = function(){
				return {"uri":this.uri, "label":this.label, "description":this.description};
			}
			
			var form = new Form();
			
			var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
            var wsSocket = new WS("ws://localhost:9000/ws");
					
			$('#uri').change(function() {
				form.uri = $(this).val();
				doPost();
			});
			
			$('#uri').keyup(function(evt) {
			    if (evt.keyCode == 32) {
			    	form.uri = $(this).val();
 			    	doPost();
 				}
			});
			
			$('#label').change(function() {
				form.label = $(this).val();
				doPost();
			});
				
			$('#label').keyup(function(evt) {
			    if (evt.keyCode == 32) {
			    	form.label = $(this).val();
 			    	doPost();
 				}
			});	
				
			$('#description').change(function() {
				form.description = $(this).val();
				doPost();
			});
			
			$('#description').keyup(function(evt) {
			    if (evt.keyCode == 32) {
			    	form.description = $(this).val();
 			    	doPost();
 				}
			});
			
			var receiveEvent = function(event) {
                var data = JSON.parse(event.data);
                var i = 1;
                
				$.each(data.recommendations, function(){
					var current = form.rank[i]
					if(current==null || current == undefined){
						form.result.append('<div class="pillb"><span class="rank">'+(i)+'</span><span class="tag" data-rank="'+(i)+'">'+this.label+'</span><span class="lang">'+this.lang+'</span></div>');
						form.rank[i]= $('span[data-rank='+i+']');
					}else{
						form.rank[i].text(this.label);
					}
					i++;
				});
            }
			
			function doPost(){

				if(form.isReady()){
					wsSocket.close();
					wsSocket = new WS("ws://localhost:9000/ws");
					wsSocket.send( JSON.stringify(form.getJson()));
				/*	$.ajax({
						type:"POST",
						url:"/recommend",
						contentType:"application/json",
						data: JSON.stringify(form.getJson()),
						complete: function(){
							
						},
						success: function(data, textStatus, jqXHR){
							var i = 1;
							$.each(data.recommendations, function(){
								form.result.append('<div class="pillb"><span class="rank">'+(i++)+'</span><span class="tag">'+this.label+'</span><span class="lang">'+this.lang+'</span></div>');
							});
						},
						error: function(jqXHR, textStatus, errorThrown){
							alert(":( "+textStatus+" "+errorThrown);
						}
					});
				*/}
			};
			
			wsSocket.onmessage = receiveEvent;
