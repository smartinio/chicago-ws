webpackJsonp([1],{"7I6i":function(t,e){},A2Hp:function(t,e){},"BI6/":function(t,e){},CUYF:function(t,e){},ErRQ:function(t,e){},IpWz:function(t,e){},MH1n:function(t,e){},NHnr:function(t,e,n){"use strict";Object.defineProperty(e,"__esModule",{value:!0});var s={};n.d(s,"BEFORE",function(){return M}),n.d(s,"TRADING",function(){return K}),n.d(s,"CHICAGO",function(){return G}),n.d(s,"PLAYING",function(){return U}),n.d(s,"AFTER",function(){return H});var a,i=n("/5sW"),r={render:function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{staticStyle:{visibility:"hidden",height:"0",width:"0"}},t._l(t.cards.suits,function(e){return n("div",t._l(t.cards.values,function(s){return n("img",{attrs:{src:t.getSrc(s,e)}})}))}))},staticRenderFns:[]},c=void 0,o={name:"app",components:{Preloader:n("VU/8")({methods:{getSrc:function(t,e){return this.getCardUrl({value:t,suit:e})}},data:function(){return{cards:{suits:{HEARTS:"HEARTS",SPADES:"SPADES",DIAMONDS:"DIAMONDS",CLUBS:"CLUBS"},values:{TWO:"TWO",THREE:"THREE",FOUR:"FOUR",FIVE:"FIVE",SIX:"SIX",SEVEN:"SEVEN",EIGHT:"EIGHT",NINE:"NINE",TEN:"TEN",JACK:"JACK",QUEEN:"QUEEN",KING:"KING",ACE:"ACE"}}}}},r,!1,null,null,null).exports},mounted:function(){c=setInterval(function(){fetch("https://chicago-server.herokuapp.com",{method:"HEAD"})},1e4)},unmounted:function(){clearInterval(c)}},l={render:function(){var t=this.$createElement,e=this._self._c||t;return e("div",{attrs:{id:"app"}},[e("router-view"),this._v(" "),e("Preloader")],1)},staticRenderFns:[]},u=n("VU/8")(o,l,!1,function(t){n("ErRQ")},null,null).exports,d=n("bOdI"),h=n.n(d),m=n("NYxO"),v="SET_MY_HAND",f="SET_MY_TURN",p="SET_MY_PLAYER",C={state:{isMyTurn:!1,hand:[],id:"",name:"",score:0,hasTakenChicago:!1,winner:!1},mutations:(a={},h()(a,v,function(t,e){t.hand=e}),h()(a,f,function(t,e){t.isMyTurn=e}),h()(a,p,function(t,e){t.id=e.id,t.name=e.name,t.score=e.score,t.hasTakenChicago=e.hasTakenChicago,t.winner=e.winner}),a)},g="MIRROR_GAME_STATE",y="HANDLE_SNAPSHOT",_=n("/ocq"),k=n("BO1k"),E=n.n(k),N=n("Zrlr"),A=n.n(N),S=function t(e,n){A()(this,t),this.nickname=e,this.key=n},R=n("mvHQ"),T=n.n(R),b=n("pFYg"),P=n.n(b),w=function t(e,n){A()(this,t),(void 0===n?"undefined":P()(n))!==P()("")&&(n=T()(n)),this.type=e,this.value=n},O={name:"Start",props:{urlKey:{type:String,default:""},errors:{type:Object,required:!0}},data:function(){return{invKey:"",nickname:""}},methods:{rememberNickname:function(){this.nickname&&localStorage.setItem("nickname",this.nickname)},createGame:function(){if(this.hasNicknameSet){var t=new w("NEW_GAME",this.nickname);this.$emit("action",t),this.rememberNickname()}},joinGame:function(){if(this.fieldsAreValid){var t=new S(this.nickname,this.invKey),e=new w("JOIN_GAME",t);this.$emit("action",e),this.rememberNickname()}},dangerIfExists:function(t){return this[t]?"is-danger":""},handleKeyupEnter:function(){this.invKey?this.joinGame():this.createGame()}},computed:{hasNicknameSet:function(){return this.nickname.length>0},hasKeySet:function(){var t=this.invKey.split("-");if(5===t.length){var e=!0,n=!1,s=void 0;try{for(var a,i=E()(t);!(e=(a=i.next()).done);e=!0){if(""===a.value)return!1}}catch(t){n=!0,s=t}finally{try{!e&&i.return&&i.return()}finally{if(n)throw s}}return!0}return!1},hasErrors:function(){return this.errors.invKey.length>0||this.errors.nickname.length>0},fieldsAreValid:function(){return!this.hasErrors&&this.hasNicknameSet&&this.hasKeySet}},created:function(){this.invKey=this.urlKey||"";var t=localStorage.getItem("nickname");t&&(this.nickname=t)},mounted:function(){this.$refs.nickname.focus()},watch:{invKey:function(){this.$emit("changeKey")},nickname:function(){this.$emit("changeNick")}}},x={render:function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("section",{staticClass:"section"},[n("div",{staticClass:"container"},[n("div",{staticClass:"field"},[n("div",{staticClass:"control"},[n("label",{staticClass:"label is-large"},[t._v("Nickname")]),t._v(" "),n("input",{directives:[{name:"model",rawName:"v-model",value:t.nickname,expression:"nickname"}],ref:"nickname",staticClass:"input is-large",class:t.dangerIfExists(t.errors.nickname),attrs:{placeholder:"Choose a nickname..."},domProps:{value:t.nickname},on:{keyup:function(e){if(!("button"in e)&&t._k(e.keyCode,"enter",13,e.key))return null;t.handleKeyupEnter(e)},input:function(e){e.target.composing||(t.nickname=e.target.value)}}}),t._v(" "),t.errors.nickname?n("p",{staticClass:"help is-danger"},[t._v("\n              "+t._s(t.errors.nickname)+"\n            ")]):t._e()])]),t._v(" "),n("div",{staticClass:"columns"},[n("div",{staticClass:"column is-expanded"},[n("div",{staticClass:"field has-addons is-fullwidth"},[n("div",{staticClass:"control is-expanded"},[n("input",{directives:[{name:"model",rawName:"v-model",value:t.invKey,expression:"invKey"}],staticClass:"input is-large",class:t.dangerIfExists(t.errors.invKey),attrs:{type:"text",placeholder:"Invitation key"},domProps:{value:t.invKey},on:{input:function(e){e.target.composing||(t.invKey=e.target.value)}}}),t._v(" "),t.errors.invKey?n("p",{staticClass:"help is-danger"},[t._v("\n                      "+t._s(t.errors.invKey)+"\n                    ")]):t._e()]),t._v(" "),n("div",{staticClass:"control"},[n("a",{staticClass:"button is-info is-large",attrs:{disabled:!t.fieldsAreValid},on:{click:t.joinGame}},[t._v("\n                    Join existing game\n                  ")])])])]),t._v(" "),n("div",{staticClass:"column is-narrow"},[n("button",{staticClass:"button is-primary is-large",attrs:{disabled:!t.hasNicknameSet},on:{click:t.createGame}},[t._v("\n                Create new game\n              ")])])])])])},staticRenderFns:[]},I=n("VU/8")(O,x,!1,function(t){n("7I6i")},null,null).exports,M="BEFORE",K="TRADING",G="CHICAGO",U="PLAYING",H="AFTER",D=n("Xxa5"),$=n.n(D),F=n("exGp"),L=n.n(F),W={render:function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("div",{staticClass:"media"},[n("div",{staticClass:"media-left"},[n("figure",{staticClass:"image",class:t.figureClass},[n("img",{staticClass:"avatar",attrs:{src:t.getAvatarUrl(t.player),alt:"Placeholder image"}})])]),t._v(" "),n("div",{staticClass:"media-content",staticStyle:{overflow:"hidden"}},[n("div",{staticClass:"columns"},[n("div",{staticClass:"column is-narrow"},[n("p",{staticClass:"title is-4"},[t._v("\n          "+t._s(t.player.name||t.fallbackName)+"\n        ")]),t._v(" "),n("p",{staticClass:"subtitle is-6"},[t._v("\n          "+t._s(t.player.score)+" points\n        ")])]),t._v(" "),n("div",{staticClass:"column is-expanded"},[t.player.hand?n("div",{staticStyle:{"padding-right":"40px"}},t._l(t.player.hand.played,function(e){return n("figure",{staticClass:"image is-inline-flex",staticStyle:{width:"66px",height:"96px","margin-right":"-40px"}},[n("img",{class:t.isBaseCard(e)?"baseCard":"",attrs:{src:t.getCardUrl(e),alt:e.type+""+e.value}})])})):t._e()]),t._v(" "),n("div",{staticClass:"column is-narrow"},[t.player.hasTakenChicago?n("span",{staticClass:"tag is-light",staticStyle:{"margin-left":"10px"}},[t._v("\n          🚀\n        ")]):t._e(),t._v(" "),!t.isMe&&t.isCurrentPlayer(t.player)?n("span",{staticClass:"tag is-info",staticStyle:{"margin-left":"10px"}},[t._v("\n          PLAYING\n        ")]):t._e(),t._v(" "),t.player.winner?n("span",{staticClass:"tag is-success",staticStyle:{"margin-left":"10px"}},[t._v("\n          WINNER\n        ")]):t._e()])])])])},staticRenderFns:[]},V=n("VU/8")({name:"Player",props:["player","baseMove","currentPlayer","fallbackName","variant","isMe"],computed:{figureClass:function(){var t="large"===this.variant?96:48;return"is-"+t+"x"+t}},methods:{isBaseCard:function(t){return!!this.baseMove&&t.suit===this.baseMove.card.suit&&t.value===this.baseMove.card.value},isCurrentPlayer:function(t){return this.currentPlayer&&this.currentPlayer.id===t.id}}},W,!1,null,null,null).exports,Y={name:"UserArea",props:["controlPlayer","baseMove","currentPlayer"],components:{Player:V}},B={render:function(){var t=this.$createElement;return(this._self._c||t)("Player",{attrs:{isMe:!0,variant:"large",fallbackName:"You",player:this.controlPlayer,baseMove:this.baseMove,currentPlayer:this.currentPlayer}})},staticRenderFns:[]},j="HANDLE_MESSAGE",J="SEND_ACTION",z={props:["game","me","controlPlayer","markedCards","currentPlayer","baseMove"],name:"Controls",components:{UserArea:n("VU/8")(Y,B,!1,null,null,null).exports},data:function(){return{copiedKey:!1,inviteText:"Invite friends",phase:s}},methods:{startGame:function(){this.doAction(new w("START_GAME"))},trade:function(){this.doAction(new w("THROW",this.markedCards))},passTrade:function(){this.doAction(new w("THROW",[]))},callChicago:function(){this.doAction(new w("CHICAGO",!0))},passChicago:function(){this.doAction(new w("CHICAGO",!1))},play:function(){this.doAction(new w("MOVE",this.markedCard))},restartGame:function(){this.doAction(new w("RESTART_GAME"))},doAction:function(t){this.$store.dispatch(J,t),this.$emit("action")},isPhase:function(t){return this.game.round.phase===t},isCurrentPlayer:function(t){return t.id===this.game.round.currentPlayer.id},copyLink:function(){var t=this;return L()($.a.mark(function e(){return $.a.wrap(function(e){for(;;)switch(e.prev=e.next){case 0:return t.tmpKey=t.game.invKey,e.next=3,navigator.clipboard.writeText(window.location.href);case 3:t.copiedKey=!0,t.inviteText="Link copied to clipboard!";case 5:case"end":return e.stop()}},e,t)}))()}},computed:{checkOrKey:function(){return this.copiedKey?"fa fa-check":"fa fa-key"},canStart:function(){return this.me.id===this.game.host.id&&this.game.players.length>1&&!this.game.started},canRestart:function(){return this.me.id===this.game.host.id&&this.game.hasWinners},markedCard:function(){return 1===this.markedCards.length&&this.markedCards[0]}}},q={render:function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("section",{staticClass:"section"},[n("div",{staticClass:"container"},[n("div",{staticClass:"columns"},[n("div",{staticClass:"column is-narrow"},[n("UserArea",{attrs:{controlPlayer:t.controlPlayer,baseMove:t.baseMove,currentPlayer:t.currentPlayer}})],1),t._v(" "),n("div",{staticClass:"column"}),t._v(" "),n("div",{staticClass:"column is-narrow"},[t.game.started?t._e():n("span",{staticClass:"tag is-medium is-primary",staticStyle:{cursor:"pointer"},on:{click:t.copyLink,mouseleave:function(e){t.inviteText="Invite friends",t.copiedKey=!1}}},[n("span",{staticClass:"icon"},[n("i",{class:t.checkOrKey})]),t._v(" "),n("span",[t._v(t._s(t.inviteText))])]),t._v(" "),t.canStart&&!t.canRestart?n("a",{staticClass:"tag is-medium",on:{click:t.startGame}},[t._v("Start game")]):t._e(),t._v(" "),t.canRestart?n("a",{staticClass:"tag is-medium is-warning",on:{click:t.restartGame}},[t._m(0),t._v(" "),n("span",[t._v("Restart")])]):t._e(),t._v(" "),t.me.isMyTurn?n("span",[t.isPhase(t.phase.TRADING)?n("span",[t.markedCards.length?n("a",{staticClass:"tag is-success is-medium",on:{click:t.trade}},[t._v("TRADE ("+t._s(t.markedCards.length)+")")]):n("a",{staticClass:"tag is-info is-medium",on:{click:t.passTrade}},[t._v("YOUR TURN (PASS)")])]):t._e(),t._v(" "),t.isPhase(t.phase.CHICAGO)?n("span",[n("span",[t._v("Want Chicago?")]),t._v(" "),n("a",{staticClass:"tag is-success is-medium",on:{click:t.callChicago}},[t._v("YES")]),t._v(" "),n("a",{staticClass:"tag is-danger is-medium",on:{click:t.passChicago}},[t._v("NO")])]):t._e(),t._v(" "),t.isPhase(t.phase.PLAYING)?n("span",[t.markedCard?n("a",{staticClass:"tag is-success is-medium",on:{click:t.play}},[t._v("PLAY CARD")]):n("span",{staticClass:"tag is-black is-medium"},[t._v("YOUR TURN")])]):t._e()]):n("span",[n("span",{staticClass:"tag is-warning is-medium"},[t._v("WAITING")])])])])])])},staticRenderFns:[function(){var t=this.$createElement,e=this._self._c||t;return e("span",{staticClass:"icon is-small"},[e("i",{staticClass:"fa fa-refresh"})])}]},Q=n("VU/8")(z,q,!1,function(t){n("CUYF")},"data-v-48304feb",null).exports,X={name:"Players",props:["players","baseMove","currentPlayer"],components:{Player:V}},Z={render:function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("section",{staticClass:"section"},[n("div",{staticClass:"container"},[n("div",{staticClass:"columns"},t._l(t.players,function(e,s){return n("div",{staticClass:"column"},[n("div",{staticClass:"card"},[n("div",{staticClass:"card-content"},[n("Player",{attrs:{isMe:!1,fallbackName:"Player "+s+1,player:e,currentPlayer:t.currentPlayer,baseMove:t.baseMove}})],1)])])}))])])},staticRenderFns:[]},tt=n("VU/8")(X,Z,!1,function(t){n("zmCB")},"data-v-0343d5e0",null).exports,et={render:function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("section",{staticClass:"section"},[n("div",{staticClass:"container"},[n("div",{staticClass:"columns is-mobile"},t._l(t.me.hand,function(e){return n("div",{staticClass:"column has-text-centered"},[n("img",{staticClass:"playingCard",class:t.getCardClasses(e),attrs:{src:t.getCardUrl(e)},on:{click:function(n){t.toggleMark(e)}}})])}))])])},staticRenderFns:[]},nt=n("VU/8")({props:["me","phase","markedCards"],name:"MyHand",methods:{toggleMark:function(t){this.$emit("toggleMark",t)},isMarked:function(t){return this.markedCards.includes(t)},getCardClasses:function(t){return this.me.isMyTurn?this.isMarked(t)?"markedCard myTurn":"myTurn":""}}},et,!1,function(t){n("BI6/")},"data-v-74bc1474",null).exports,st=n("d7EF"),at=n.n(st),it=n("7nRM"),rt=n.n(it),ct={CLUBS:"♣️",HEARTS:"❤️",DIAMONDS:"♦️",SPADES:"♠️"},ot=function(t){var e=t.split(""),n=rt()(e),s=n[0],a=n.slice(1);return s.toUpperCase()+a.join("").toLowerCase()},lt=function(t){var e=ot(t.value);return ct[t.suit]+" "+e},ut={props:["game"],name:"EventLog",computed:{events:function(){return this.game.events}},watch:{events:function(){var t=this;requestAnimationFrame(function(){var e=(t.$refs.rows||[]).slice(-1),n=at()(e,1)[0];n&&n.scrollIntoView({behavior:"smooth"})})}},methods:{timeago:n("8dtK").a,formatServerEvent:function(t){switch(t.action){case"NEW_ROUND":return"✨ A new round begins ✨";case"TRICK_DONE":return"👀"}},formatPlayerEvent:function(t){switch(t.action){case"TRADED":var e=1===t.numCards?"card":"cards",n=0===t.numCards?"🤔":"🫳";return"traded "+t.numCards+" "+e+" "+n;case"PLAYED":return"played "+lt(t.card);case"WON_TRICK":return"won the trick!";case"CALLED_CHICAGO":return"called Chicago! 🚀";case"LOST_CHICAGO":return"lost their Chicago... 🥲";case"WON_CHICAGO":return"won their Chicago! 🥳";case"WON_ROUND":return"won the round! 🙌";case"WON_BEST_HAND":var s=ot(t.handType.replaceAll("_"," ")),a=1===t.points?"point":"points";return"got "+t.points+" "+a+" for a "+s+" 💰";case"WON_GAME":return"won the game! 👑 👑 👑";case"WON_ROUND_GUARANTEED":return"made it rain 💦 with "+t.cards.map(lt).join(", ")}}}},dt={render:function(){var t=this,e=t.$createElement,n=t._self._c||e;return n("section",{staticClass:"section pb-0 pt-0",staticStyle:{position:"relative"}},[n("div",{staticClass:"container"},[n("div",{staticClass:"disable-scrollbars content is-flex is-justify-content-flex-start is-flex-direction-column",staticStyle:{overflow:"scroll",height:"300px",width:"400px","padding-top":"40px"}},t._l(t.events,function(e){return n("div",["server"===e.actor.id&&t.formatServerEvent(e)?n("div",{staticStyle:{"border-bottom":"1px solid #ccc",height:"15px","text-align":"center","margin-bottom":"15px"}},[n("span",{staticClass:"has-text-grey is-size-7",staticStyle:{height:"40px","background-color":"#fff",padding:"5px"}},[t._v("\n            "+t._s(t.formatServerEvent(e))+"\n          ")])]):t.formatPlayerEvent(e)?n("div",[n("strong",[t._v(t._s(e.actor.name))]),t._v(" "),n("span",[t._v(" "+t._s(t.formatPlayerEvent(e)))]),t._v(" "),n("span",{staticClass:"has-text-grey-light is-size-7",staticStyle:{"line-height":"30px"}},[t._v(t._s(t.timeago(e.timestamp)))])]):t._e(),t._v(" "),n("span",{ref:"rows",refInFor:!0})])}))]),t._v(" "),n("div",{staticClass:"fade-overlay"})])},staticRenderFns:[]},ht=n("VU/8")(ut,dt,!1,function(t){n("MH1n")},"data-v-6e695c82",null).exports,mt={name:"Game",props:{game:{type:Object,required:!0},me:{type:Object,required:!0}},components:{Controls:Q,Players:tt,MyHand:nt,EventLog:ht},data:function(){return{markedCards:[]}},methods:{toggleMark:function(t){this.me.isMyTurn&&!this.isPhase(G)?this.isPhase(U)?this.markSingle(t):this.isMarked(t)?this.unmark(t):this.mark(t):this.unmarkAll()},unmark:function(t){this.markedCards=this.markedCards.filter(function(e){return e!==t})},unmarkAll:function(){this.markedCards=[]},mark:function(t){this.markedCards.push(t)},markSingle:function(t){this.markedCards=[t]},isMarked:function(t){return this.markedCards.includes(t)},isPhase:function(t){return this.game.round.phase===t}},computed:{baseMove:function(){if(this.currentTrick)return this.currentTrick.moves&&this.currentTrick.moves.length>0&&this.currentTrick.moves[0]},currentTrick:function(){var t=this.game.round,e=t&&t.tricks;if(e)return e.length>0&&e[e.length-1]},currentPlayer:function(){return this.game.round&&this.game.round.currentPlayer},controlPlayer:function(){var t=this;return this.game.players.find(function(e){return e.id===t.me.id})},otherPlayers:function(){var t=this;return this.game.players.filter(function(e){return e.id!==t.me.id})}}},vt={render:function(){var t=this.$createElement,e=this._self._c||t;return e("div",[e("Controls",{attrs:{game:this.game,me:this.me,controlPlayer:this.controlPlayer,currentPlayer:this.currentPlayer,baseMove:this.baseMove,markedCards:this.markedCards},on:{action:this.unmarkAll}}),this._v(" "),e("section",{staticClass:"section pb-0 pt-0 is-flex is-flex-direction-row"},[e("EventLog",{attrs:{game:this.game}}),this._v(" "),this.game.started?e("MyHand",{attrs:{me:this.me,markedCards:this.markedCards},on:{toggleMark:this.toggleMark}}):this._e()],1),this._v(" "),this.otherPlayers?e("Players",{attrs:{players:this.otherPlayers,baseMove:this.baseMove,currentPlayer:this.currentPlayer}}):this._e()],1)},staticRenderFns:[]},ft=n("VU/8")(mt,vt,!1,function(t){n("TDG6")},null,null).exports,pt={props:{connected:{type:Boolean,default:!1}},methods:{handleClick:function(){this.connected||this.$emit("reconnect")}},computed:{statusText:function(){return this.connected?"Connected to server":"Not connected. Click to try again"},statusClass:function(){return this.connected?"is-success":"is-danger"},statusIcon:function(){return this.connected?"fa-check":"fa-refresh"}}},Ct={render:function(){var t=this.$createElement,e=this._self._c||t;return e("div",{staticClass:"container breathe"},[e("a",{staticClass:"button is-pulled-right is-small",class:this.statusClass,on:{click:this.handleClick}},[e("span",{staticClass:"icon is-small"},[e("i",{staticClass:"fa",class:this.statusIcon})]),this._v(" "),e("span",[this._v(this._s(this.statusText))])])])},staticRenderFns:[]},gt="SET_NICKNAME_ERROR",yt="SET_INVKEY_ERROR",_t={components:{Start:I,Game:ft,ConnectionStatus:n("VU/8")(pt,Ct,!1,function(t){n("IpWz")},null,null).exports},name:"Wrapper",computed:Object(m.b)({game:"game",me:"me",errors:"errors",socket:"socket"}),methods:{dispatchAction:function(t){this.$store.dispatch(J,t)},clearKeyError:function(){this.$store.commit(yt,"")},clearNickError:function(){this.$store.commit(gt,"")},connect:function(){this.$store.dispatch("CONNECT")}},created:function(){this.connect()}},kt={render:function(){var t=this.$createElement,e=this._self._c||t;return e("div",[e("ConnectionStatus",{attrs:{connected:this.socket.connected},on:{reconnect:this.connect}}),this._v(" "),this.game.invKey?e("Game",{attrs:{me:this.me,game:this.game}}):e("Start",{attrs:{errors:this.errors,urlKey:this.$route.params.key},on:{action:this.dispatchAction,changeKey:this.clearKeyError,changeNick:this.clearNickError}})],1)},staticRenderFns:[]},Et=n("VU/8")(_t,kt,!1,function(t){n("A2Hp")},"data-v-41efd68f",null).exports;i.a.use(_.a);var Nt,At,St,Rt,Tt,bt=[{path:"/:key",component:Et},{path:"/",component:Et}],Pt=new _.a({base:"/",routes:bt,mode:"hash"}),wt={state:{host:{id:""},invKey:"",events:[],players:[],started:!1,round:{currentPlayer:"",phase:"",tricks:[]}},mutations:h()({},g,function(t,e){t.host.id=e.host.id,t.invKey=e.invitationKey,t.players=e.players,t.events=e.events,t.started=e.started,t.hasWinners=e.hasWinners,e.currentRound&&(t.round.currentPlayer=e.currentRound.currentPlayer,t.round.phase=e.currentRound.phase,t.round.tricks=e.currentRound.tricks)}),actions:h()({},y,function(t,e){var n=t.commit,s=JSON.parse(e);Pt.push(s.game.invitationKey),n(f,s.myTurn),n(v,s.myHand),n(p,s.me),n(g,s.game)})},Ot="HANDLE_GAME_ERROR",xt="HANDLE_JSON_ERROR",It={state:{nickname:"",invKey:""},mutations:(Nt={},h()(Nt,gt,function(t,e){t.nickname=e}),h()(Nt,yt,function(t,e){t.invKey=e}),Nt),actions:(At={},h()(At,Ot,function(t,e){t.state;window.alert(e)}),h()(At,xt,function(t,e){t.state;window.alert("Something went wrong! Please try again")}),At)},Mt="SET_CONNECTED_TRUE",Kt="SET_CONNECTED_FALSE",Gt=function t(e){A()(this,t);var n=JSON.parse(e.data),s=n.type,a=n.body;this.type=s,this.body=a},Ut={connected:!1,socket:void 0},Ht=(St={},h()(St,Mt,function(t){t.connected=!0}),h()(St,Kt,function(t){t.connected=!1}),h()(St,"SET_SOCKET",function(t,e){t.socket=e}),St),Dt={state:Ut,actions:(Rt={},h()(Rt,"CONNECT",function(t){var e=t.commit,n=t.dispatch,s=new WebSocket("wss://chicago-server.herokuapp.com/game"),a=void 0;s.onopen=function(){var t=T()(new w("PING","pong"));a=setInterval(function(){return s.send(t)},1e4),e(Mt)},s.onclose=function(){clearInterval(a),e(Kt)},s.onmessage=function(t){n(j,t)},e("SET_SOCKET",s)}),h()(Rt,j,function(t,e){var n=t.commit,s=t.dispatch,a=new Gt(e);switch(a.type){case"KEY_ERROR":n(yt,a.body);break;case"NICKNAME_ERROR":n(gt,a.body);break;case"GAME_ERROR":s(Ot,a.body);break;case"JSON_ERROR":s(xt,a.body);break;case"SNAPSHOT":s(y,a.body)}}),h()(Rt,J,function(t,e){t.state.socket.send(T()(e))}),Rt),mutations:Ht};i.a.use(m.a);var $t=new m.a.Store({modules:(Tt={},h()(Tt,"me",C),h()(Tt,"game",wt),h()(Tt,"errors",It),h()(Tt,"socket",Dt),Tt)});i.a.config.productionTip=!1,i.a.mixin({methods:{getCardUrl:function(t){return"static/cards/"+t.value+"_"+t.suit+".svg"},getAvatarUrl:function(t){return"https://avatars.dicebear.com/api/adventurer-neutral/"+t.id+".svg"}}}),new i.a({store:$t,router:Pt,el:"#app",render:function(t){return t(u)}}).$mount("#app")},TDG6:function(t,e){},zmCB:function(t,e){}},["NHnr"]);
//# sourceMappingURL=app.91c156f81d4bbb8522fd.js.map