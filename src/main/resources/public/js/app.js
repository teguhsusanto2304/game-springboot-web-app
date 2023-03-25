$( document ).ready( function() {

	turn = $("#turn").val();
	username = $("#username").text();
	console.log('username length: ' + username.length+', turn: '+turn );

	if(username.length>0){
		console.log('invoke konek() ' );
	//if username exist , means this is the realtime game with other HUMAN player, the we establish websocket connection
	konek();
	}



	if( $( "#is_game_over" ).val() !== "true" ) {
		$( ".board-row-tile.available" ).click( function( event ) {

			console.log('click username  : ' +username +',turn: '+turn);


					if(username.length>0){
						// this is click square action for HUMAN game type
						if(turn==='true'){
								$( "#tile_id" ).val( event.target.id );
								$( "#form_mark_tile" ).submit();
								console.log('click submitting form ' );

							console.log('stompClient send move  ' );
							// also send realtime move message to other player to refresh His tic tac toe
					        stompClient.send("/app/move", {}, JSON.stringify({'name':username,'tileId': event.target.id ,'messageType': 'MOVE'}));
					    } // end turn
				    } else {
				       	// this is click square action for COMPUTER game type
						$( "#tile_id" ).val( event.target.id );
						$( "#form_mark_tile" ).submit();
						console.log('click submitting form ' );
				    }


		} );
	}

	$( "#btn-new-game" ).click( function( event ) {
		$( "#new_game" ).val( "yes" );
		$( "#form_mark_tile" ).submit();
	} );

} );



function konek() {
	console.log('connecting  ...  '  );
	var socket = new SockJS('/websocket');
	stompClient = Stomp.over(socket);

	stompClient.connect({}, function (frame) {
		console.log('Connected  : ' + frame);
		stompClient.subscribe('/topic/move',
				function (actionMessage) {
			console.log("actionMessage.body: "+actionMessage.body);
			showActionMessage(JSON.parse(actionMessage.body));
				});
		console.log('stompClient sending JOIN message ' );
		stompClient.send("/app/move", {}, JSON.stringify({'name':username,'messageType': 'JOIN'}));

	});
}

function showActionMessage(actionMessage){
	console.log("showActionMessage username: "+username+", actionMessage.name: "+actionMessage.name );

	if(actionMessage.messageType === 'JOIN') {
		console.log("actionMessage "+actionMessage.name+" has joined ");

		if(actionMessage.name!=username){
		    $("#message").html('<p>'+actionMessage.name+' has joined</p>');
	    }
	}else if(actionMessage.messageType === 'LEAVE'){
		console.log("actionMessage "+actionMessage.name+" has left ");
		if(actionMessage.name!=username){
			$("#message").html('<p>'+actionMessage.name+' has left</p>');
		}
	}else{
		//console.log("message text has move ");
		self='true';
		if(actionMessage.name!==username){
		   self='false';
		}
		console.log("actionMessage self: "+self);
		if(self==='false') {
			console.log("actionMessage playMove ");
			// if self false, means this is realtime message to display His latest move on player screen
			// call ajax GET request to refresh our screen to show other player latest move
	        $.ajax({
	            method: 'GET',
	            url: 'http://localhost:8080/playMove?tile_id='+ actionMessage.tileId+'&new_game=false&player_go_first=false&self=false',
	            contentType: "application/json; charset=utf-8",
	                  success: function (msg)
	                          {		window.location.href = "http://localhost:8080/play";},
	                  error: function (err)
	                  {  }
	        });
		}
 	}
}