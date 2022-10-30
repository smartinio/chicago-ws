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
        <a class="tag is-danger is-medium deal-pulse" @click="deal">DEAL CARDS</a>
      </span>
      <span v-else-if="me.isMyTurn && !isPhase(phase.AFTER)">
        <span class="tag is-info is-medium">YOUR TURN</span>
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
import { START_GAME, DEAL_CARDS, RESTART_GAME } from '@/dto/action/types'
import Action from '@/dto/action/Action'
import { SEND_ACTION } from '@/store/modules/socket/action_types'
import * as phaseTypes from '@/store/modules/game/phase_types'
import PlayerStatus from '@/views/components/PlayerStatus'
const yourTurn = new Audio('static/audio/yourturn.ogg');

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
      phase: phaseTypes,
      active: true,
    }
  },
  watch: {
    ['me.isMyTurn'](isMyTurn) {
      if (isMyTurn && this.isPhase(phaseTypes.PLAYING) && !this.active) {
        yourTurn.play();
      }
    }
  },
  created () {
    window.onblur = () => {
      this.active = false
    }
    window.onfocus = () => {
      this.active = true
    }
  },
  methods: {
    startGame () {
      this.doAction(new Action(START_GAME))
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
      return this.markedCards.length == 1
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
.deal-pulse {
  animation: shadow-pulse 0.5s infinite ease-in-out alternate;
}

@keyframes shadow-pulse {
  from {
    scale: 1;
    box-shadow: 0 8px 20px rgba(255, 0, 0, 0);
  }
  to {
    scale: 1.05;
    box-shadow: 0 8px 20px rgba(255, 0, 0, 0.6);
  }
}
</style>
