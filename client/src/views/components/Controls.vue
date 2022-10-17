<template>
  <section class="section">
    <div class="container">
      <div class="columns">
        <div class="column is-narrow">
          <UserArea
            :user="me"
            :baseMove="baseMove"
            :currentPlayer="currentPlayer"
          />
        </div>
        <div class="column"></div>
        <div class="column is-narrow">
          <span
            v-if="!game.started"
            class="tag is-medium is-primary"
            @click="copyKeyToClipboard"
            @mouseover="inviteText = ''"
            @mouseleave="inviteText = 'Invite friends'"
          >
            <span class="icon"><i :class="checkOrKey"></i></span>
            <span ref="keySpan">{{ inviteText || game.invKey }}</span>
          </span>
          <a class="tag is-medium" v-if="canStart && !canRestart" @click="startGame">Start game</a>
          <a class="tag is-medium is-warning" v-if="canRestart" @click="restartGame">
            <span class="icon is-small"><i class="fa fa-refresh"></i></span>
            <span>Restart</span>
          </a>
          <span v-if="me.isMyTurn">
            <span v-if="isPhase(phase.TRADING)">
              <a class="tag is-info is-medium" v-if="!markedCards.length" @click="passTrade">YOUR TURN (PASS)</a>
              <a class="tag is-success is-medium" v-else @click="trade">TRADE ({{ markedCards.length }})</a>
            </span>
            <span v-if="isPhase(phase.CHICAGO)">
              <span>Want Chicago?</span>
              <a class="tag is-success is-medium" @click="callChicago">YES</a>
              <a class="tag is-danger is-medium" @click="passChicago">NO</a>
            </span>
            <span v-if="isPhase(phase.PLAYING)">
              <a class="tag is-success is-medium" v-if="markedCard" @click="play">PLAY CARD</a>
              <span class="tag is-black is-medium" v-else>YOUR TURN</span>
            </span>
          </span>
          <span v-else>
            <span class="tag is-warning is-medium">WAITING</span>
          </span>
        </div>
      </div>
    </div>
  </section>
</template>
<script>
import { START_GAME, THROW, CHICAGO, MOVE, RESTART_GAME } from '@/dto/action/types'
import UserArea from './UserArea'
import Action from '@/dto/action/Action'
import { SEND_ACTION } from '@/store/modules/socket/action_types'
import * as phaseTypes from '@/store/modules/game/phase_types'

export default {
  props: ['game', 'me', 'markedCards', 'currentPlayer', 'baseMove'],
  name: 'Controls',
  components: {
    UserArea
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
    isCurrentPlayer (player) {
      return player.id === this.game.round.currentPlayer.id
    },
    copyKeyToClipboard () {
      this.selectElementText(this.$refs.keySpan)
      document.execCommand('copy')
      this.copiedKey = true
      // TODO: deselect text after copy
    },
    selectElementText (el) {
      const win = window
      let doc = win.document, sel, range
      if (win.getSelection && doc.createRange) {
        sel = win.getSelection()
        range = doc.createRange()
        range.selectNodeContents(el)
        sel.removeAllRanges()
        sel.addRange(range)
      } else if (doc.body.createTextRange) {
        range = doc.body.createTextRange()
        range.moveToElementText(el)
        range.select()
      }
    }
  },
  computed: {
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
