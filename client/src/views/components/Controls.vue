<template>
  <div>
    <div class="is-flex is-flex-direction-row is-justify-content-flex-end" style="gap: 10px">
      <span
        v-if="!game.started"
        class="tag is-medium is-primary"
        @click="copyLink"
        @mouseleave="inviteText = 'Invite friends'; copiedKey = false"
        style="cursor: pointer"
      >
        <span class="icon"><i :class="checkOrKey"></i></span>
        <span>{{ inviteText }}</span>
      </span>
      <a class="tag is-medium" v-if="canStart && !canRestart" @click="startGame">Start game</a>
      <a class="tag is-medium is-warning" v-if="canRestart" @click="restartGame">
        <span class="icon is-small"><i class="fa fa-refresh"></i></span>
        <span>Restart</span>
      </a>
      <span v-if="me.imDealing && isPhase(phase.AFTER)">
        <a class="tag is-danger is-medium" @click="deal">DEAL CARDS</a>
      </span>
      <span v-else-if="me.isMyTurn">
        <span v-if="isPhase(phase.TRADING) && canRespondToOpenCard">
          <span>Want {{ niceCard(game.oneOpen.card) }}?</span>
          <a class="tag is-info is-medium" @click="respondToOneOpen(true)">
            KEEP
          </a>
          <a class="tag is-warning is-medium" @click="respondToOneOpen(false)">
            THROW
          </a>
        </span>
        <span v-else-if="isPhase(phase.TRADING) && canTradeOpenly">
          <a class="tag is-success is-medium" @click="trade">TRADE (1)</a>
          <a class="tag is-info is-medium" @click="tradeOpenly">OPEN (1)</a>
        </span>
        <span v-else-if="isPhase(phase.TRADING)">
          <a class="tag is-info is-medium" v-if="!markedCards.length" @click="passTrade">YOUR TURN (PASS)</a>
          <a class="tag is-success is-medium" v-else @click="trade">TRADE ({{ markedCards.length }})</a>
        </span>
        <span v-else-if="isPhase(phase.CHICAGO)">
          <span>Want Chicago?</span>
          <a class="tag is-success is-medium" @click="callChicago">YES</a>
          <a class="tag is-danger is-medium" @click="passChicago">NO</a>
        </span>
        <span v-else-if="isPhase(phase.PLAYING)">
          <a class="tag is-success is-medium" v-if="markedCard" @click="play">PLAY {{niceCard(markedCard)}}</a>
          <span class="tag is-black is-medium" v-else>YOUR TURN</span>
        </span>
      </span>
      <span v-else>
        <span class="tag is-warning is-medium">WAITING</span>
      </span>
    </div>
    <div style="margin-top: 10px" class="is-flex is-justify-content-flex-end">
      <PlayerStatus
        :isMe="true"
        :player="controlPlayer"
        :dealer="dealer"
        :currentPlayer="currentPlayer"
      />
    </div>
  </div>
</template>
<script>
import { START_GAME, THROW, THROW_ONE_OPEN, RESPOND_ONE_OPEN, CHICAGO, MOVE, DEAL_CARDS, RESTART_GAME } from '@/dto/action/types'
import Action from '@/dto/action/Action'
import { SEND_ACTION } from '@/store/modules/socket/action_types'
import * as phaseTypes from '@/store/modules/game/phase_types'
import PlayerStatus from '@/views/components/PlayerStatus'

export default {
  props: ['game', 'me', 'dealer', 'controlPlayer', 'markedCards', 'currentPlayer', 'baseMove'],
  name: 'Controls',
  components: {
    PlayerStatus,
  },
  data () {
    return {
      copiedKey: false,
      inviteText: 'Invite friends',
      phase: phaseTypes
    }
  },
  methods: {
    startGame () {
      this.doAction(new Action(START_GAME))
    },
    trade () {
      this.doAction(new Action(THROW, this.markedCards))
    },
    tradeOpenly () {
      const [openCard] = this.markedCards
      this.doAction(new Action(THROW_ONE_OPEN, openCard))
    },
    respondToOneOpen (accepted) {
      this.doAction(new Action(RESPOND_ONE_OPEN, accepted))
    },
    passTrade () {
      this.doAction(new Action(THROW, []))
    },
    callChicago () {
      this.doAction(new Action(CHICAGO, true))
    },
    passChicago () {
      this.doAction(new Action(CHICAGO, false))
    },
    play () {
      this.doAction(new Action(MOVE, this.markedCard))
    },
    deal () {
      this.doAction(new Action(DEAL_CARDS))
    },
    restartGame () {
      this.doAction(new Action(RESTART_GAME))
    },
    doAction (actionDTO) {
      this.$store.dispatch(SEND_ACTION, actionDTO)
      this.$emit('action')
    },
    isPhase (phase) {
      return this.game.round.phase === phase
    },
    async copyLink () {
      this.tmpKey = this.game.invKey
      await navigator.clipboard.writeText(window.location.href)
      this.copiedKey = true
      this.inviteText = 'Link copied to clipboard!'
    },
  },
  computed: {
    canTradeOpenly () {
      return this.markedCards.length == 1 && this.game.round.isFinalTrade
    },
    canRespondToOpenCard () {
      return this.game.oneOpen.isOpen
    },
    checkOrKey () {
      return this.copiedKey ? 'fa fa-check' : 'fa fa-key'
    },
    canStart () {
      return this.me.id === this.game.host.id &&
        this.game.players.length > 1 &&
        !this.game.started
    },
    canRestart () {
      return (this.me.id === this.game.host.id) && this.game.hasWinners
    },
    markedCard () {
      return this.markedCards.length === 1 && this.markedCards[0]
    }
  }
}
</script>
<style lang="scss" scoped>
</style>
