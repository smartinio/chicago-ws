<template>
  <div class="is-flex" style="overflow-y: hidden">
    <div style="transform: translateY(100px)"
      class="is-flex is-flex-direction-column is-justify-content-flex-end is-align-items-center">
      <div v-if="phase === 'CHICAGO' && me.isMyTurn" class="is-flex is-flex-direction-column is-align-items-center">
        <span class="control-text" style="margin-bottom: 15px; margin-right: 0px;">
          Want Chicago?
        </span>
        <div class="is-flex">
          <button @click="respondToChicago(true)" class="control-button button is-success is-small is-rounded"
            :disabled="!me.isMyTurn">
            YES
          </button>
          <div style="width: 10px"></div>
          <button @click="respondToChicago(false)" class="control-button button is-danger is-small is-rounded"
            style="margin-bottom: 15px;" :disabled="!me.isMyTurn">
            NO
          </button>
        </div>
      </div>
      <div v-if="phase === 'TRADING' && canSeeOpenCard" class="is-flex is-flex-direction-column is-align-items-center">
        <div class="control-text" style="margin-bottom: 15px; margin-right: 0px; overflow: visible">
          <img :src="getCardUrl(game.oneOpen.card)" style="maxWidth: 64px" class="open-card" />
        </div>
        <div class="is-flex" v-if="canRespondToOpenCard">
          <button @click="respondToOneOpen(true)" class="control-button button is-success is-small is-rounded"
            :disabled="!me.isMyTurn">
            ACCEPT
          </button>
          <div style="width: 10px"></div>
          <button @click="respondToOneOpen(false)" class="control-button button is-danger is-small is-rounded"
            style="margin-bottom: 15px;" :disabled="!me.isMyTurn">
            DECLINE
          </button>
        </div>
        <div v-else class="control-text">
          Open offer for <strong>{{ game.oneOpen.player.name }}</strong>
        </div>
      </div>
      <div v-else-if="phase === 'TRADING' && canTradeOpenly">
        <button @click="trade" class="control-button button is-success is-small is-rounded" style="margin-bottom: 15px;"
          :disabled="!me.isMyTurn">
          TRADE (1)
        </button>
        <span style="width: 5px; display: inline-block" />
        <button @click="tradeOpenly" class="control-button button is-info is-small is-rounded" style="margin-bottom: 15px;"
          :disabled="!me.isMyTurn">
          OPEN (1)
        </button>
      </div>
      <div v-else-if="phase === 'TRADING' && markedCards.length">
        <button class="control-button button is-small is-rounded is-success" style="margin-bottom: 15px;"
          :disabled="!me.isMyTurn" @click="trade">
          TRADE ({{ markedCards.length }})
        </button>
      </div>
      <div v-else-if="phase === 'TRADING'">
        <button class="control-button button is-small is-rounded is-info" style="margin-bottom: 15px;"
          :disabled="!me.isMyTurn" @click="trade">
          PASS
        </button>
      </div>
      <div :style="canReleaseCards ? 'opacity: 1' : 'opacity: 0.4'" class="is-flex">
        <div v-for="card in me.hand" class="is-flex is-flex-direction-column is-align-items-center">
          <button v-if="phase === 'PLAYING'" class="button is-small is-rounded is-success control-button"
            :class="getCardButtonClass(card)" style="margin-bottom: 15px;" @click="play(card)" :disabled="!me.isMyTurn">
            PLAY
          </button>
          <img @mousedown="toggleMark(card)" class="playingCard" :class="getCardClass(card)" :src="getCardUrl(card)" :draggable="false" />
        </div>
      </div>
    </div>
  </div>
</template>
<script lang="ts">
import { MOVE, THROW, THROW_ONE_OPEN, RESPOND_ONE_OPEN, CHICAGO } from '@/dto/action/types'
import { SEND_ACTION } from '@/store/modules/socket/action_types'
import Action from '@/dto/action/Action'

const isCard = (a) => (b) => a.suit === b.suit && a.value === b.value

export default {
  props: ['me', 'game', 'phase', 'markedCards'],
  name: 'MyHand',
  computed: {
    canReleaseCards() {
      return this.canSelectCards && this.me.isMyTurn
    },
    canSelectCards() {
      return this.phase !== 'AFTER' && !this.canRespondToOpenCard
    },
    canTradeOpenly() {
      if (this.game.rules.oneOpen === 'ALL') {
        return this.markedCards.length == 1
      }
      if (this.game.rules.oneOpen === 'FINAL') {
        return this.markedCards.length == 1 && this.game.round.isFinalTrade
      }
    },
    canSeeOpenCard() {
      return this.game.oneOpen.isOpen
    },
    canRespondToOpenCard() {
      return this.canSeeOpenCard && this.game.oneOpen.player.id === this.me.id
    },
  },
  methods: {
    toggleMark(card) {
      if (!this.canSelectCards) return;
      this.$emit('toggleMark', card)
    },
    isMarked(card) {
      return this.markedCards.some(isCard(card))
    },
    getCardClass(card) {
      return this.isMarked(card) ? 'markedCard' : ''
    },
    getCardButtonClass(card) {
      return this.isMarked(card) ? 'markedCard' : 'invis'
    },
    getTradeButtonClass() {
      return this.markedCards.length ? 'markedCard' : 'invis'
    },
    doAction(actionDTO) {
      this.$store.dispatch(SEND_ACTION, actionDTO)
      this.$emit('action')
    },
    play(card) {
      if (!this.me.isMyTurn) return;
      this.doAction(new Action(MOVE, card));
    },
    trade() {
      if (!this.me.isMyTurn) return;
      this.doAction(new Action(THROW, this.markedCards));
    },
    tradeOpenly() {
      if (!this.me.isMyTurn || this.markedCards.length !== 1) return;
      this.doAction(new Action(THROW_ONE_OPEN, this.markedCards[0]));
    },
    respondToOneOpen(accepted) {
      this.doAction(new Action(RESPOND_ONE_OPEN, accepted))
    },
    respondToChicago(accepted) {
      this.doAction(new Action(CHICAGO, accepted))
    },
  }
}
</script>
<style lang="scss" scoped>
.control-button {
  transform: translateY(-60px);
  max-height: 30vh;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.open-card {
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.control-text {
  transform: translateY(-60px);
  max-height: 30vh;
}

.playingCard {
  transform: translateY(0px);
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
  max-height: 30vh;
}

.markedCard {
  transform: translateY(-60px);
  box-shadow: 0px 8px 20px rgba(0, 0, 0, 0.4);
  transition: all 0.3s ease;
  max-height: 30vh;
}

.myTurn {
  opacity: 1;
}

.invis {
  opacity: 0;
}
</style>
