<template>
  <div style="transform: translateY(100px)" class="is-flex is-flex-direction-column is-justify-content-flex-end is-align-items-center">
    <div v-if="phase === 'CHICAGO' && me.isMyTurn" class="is-flex is-flex-direction-column is-align-items-center">
      <span
        class="control-text"
        style="margin-bottom: 15px; margin-right: 0px;"
      >
        Want Chicago?
      </span>
      <div class="is-flex">
        <a
          @click="respondToChicago(true)"
          class="control-button button is-success is-small is-rounded"
          :disabled="!me.isMyTurn"
        >
          YES
        </a>
        <div style="width: 10px"></div>
        <a
          @click="respondToChicago(false)"
          class="control-button button is-danger is-small is-rounded"
          style="margin-bottom: 15px;"
          :disabled="!me.isMyTurn"
        >
          NO
        </a>
      </div>
    </div>
    <div v-if="phase === 'TRADING' && canRespondToOpenCard" class="is-flex is-flex-direction-column is-align-items-center">
      <div
        class="control-text"
        style="margin-bottom: 15px; margin-right: 0px; overflow: visible"
      >
        <img :src="getCardUrl(game.oneOpen.card)" style="maxWidth: 64px" class="open-card" />
      </div>
      <div class="is-flex">
        <a
          @click="respondToOneOpen(true)"
          class="control-button button is-success is-small is-rounded"
          :disabled="!me.isMyTurn"
        >
          ACCEPT
        </a>
        <div style="width: 10px"></div>
        <a
          @click="respondToOneOpen(false)"
          class="control-button button is-danger is-small is-rounded"
          style="margin-bottom: 15px;"
          :disabled="!me.isMyTurn"
        >
          DECLINE
        </a>
      </div>
    </div>
    <div v-else-if="phase === 'TRADING' && canTradeOpenly">
      <a
        @click="trade"
        class="control-button button is-success is-small is-rounded"
        style="margin-bottom: 15px;"
        :disabled="!me.isMyTurn"
      >
        TRADE (1)
      </a>
      <a
        @click="tradeOpenly"
        class="control-button button is-info is-small is-rounded"
        style="margin-bottom: 15px;"
        :disabled="!me.isMyTurn"
      >
        OPEN (1)
      </a>
    </div>
    <div v-else-if="phase === 'TRADING' && markedCards.length">
      <a
        class="control-button button is-small is-rounded is-success"
        style="margin-bottom: 15px;"
        :disabled="!me.isMyTurn"
        @click="trade"
      >
        TRADE ({{ markedCards.length }})
      </a>
    </div>
    <div v-else-if="phase === 'TRADING'">
      <a
        class="control-button button is-small is-rounded is-info"
        style="margin-bottom: 15px;"
        :disabled="!me.isMyTurn"
        @click="trade"
      >
        PASS
      </a>
    </div>
    <div :style="me.isMyTurn ? 'opacity: 1' : 'opacity: 0.4'" class="is-flex">
      <div
        v-for="card in me.hand"
        class="is-flex is-flex-direction-column is-align-items-center"
      >
        <a
          v-if="phase === 'PLAYING'"
          class="button is-small is-rounded is-success control-button"
          :class="getCardButtonClass(card)"
          style="margin-bottom: 15px;"
          @click="play(card)"
          :disabled="!me.isMyTurn"
        >
          PLAY
        </a>
        <img
          @click="toggleMark(card)"
          class="playingCard"
          :class="getCardClass(card)"
          :src="getCardUrl(card)"
        />
      </div>
    </div>
  </div>
</template>
<script>
import { MOVE, THROW, THROW_ONE_OPEN, RESPOND_ONE_OPEN, CHICAGO } from '@/dto/action/types'
import { SEND_ACTION } from '@/store/modules/socket/action_types'
import Action from '@/dto/action/Action'

const isCard = (a) => (b) => a.suit === b.suit && a.value === b.value

export default {
  props: ['me', 'game', 'phase', 'markedCards'],
  name: 'MyHand',
  computed: {
    canTradeOpenly () {
      return this.markedCards.length == 1
    },
    canRespondToOpenCard () {
      return this.game.oneOpen.isOpen
    },
  },
  methods: {
    toggleMark (card) {
      if (this.canRespondToOpenCard) return;
      this.$emit('toggleMark', card)
    },
    isMarked (card) {
      return this.markedCards.some(isCard(card))
    },
    getCardClass (card) {
      return this.isMarked(card) ? 'markedCard' : ''
    },
    getCardButtonClass (card) {
      return this.isMarked(card) ? 'markedCard' : 'invis'
    },
    getTradeButtonClass () {
      return this.markedCards.length ? 'markedCard' : 'invis'
    },
    doAction (actionDTO) {
      this.$store.dispatch(SEND_ACTION, actionDTO)
      this.$emit('action')
    },
    play (card) {
      if (!this.me.isMyTurn) return;
      this.doAction(new Action(MOVE, card));
    },
    trade () {
      if (!this.me.isMyTurn) return;
      this.doAction(new Action(THROW, this.markedCards));
    },
    tradeOpenly () {
      if (!this.me.isMyTurn || this.markedCards.length !== 1) return;
      this.doAction(new Action(THROW_ONE_OPEN, this.markedCards[0]));
    },
    respondToOneOpen (accepted) {
      this.doAction(new Action(RESPOND_ONE_OPEN, accepted))
    },
    respondToChicago (accepted) {
      this.doAction(new Action(CHICAGO, accepted))
    },
  }
}
</script>
<style lang="scss" scoped>
  .control-button {
    transform: translateY(-60px);
    max-height: 30vh;
    box-shadow: 0 2px 10px rgba(0,0,0,0.2);
  }
  .open-card {
    box-shadow: 0 2px 10px rgba(0,0,0,0.2);
  }
  .control-text {
    transform: translateY(-60px);
    max-height: 30vh;
  }
  .playingCard {
    transform: translateY(0px);
    box-shadow: 0 0 5px rgba(0,0,0,0.1);
    transition: all 0.2s ease;
    max-height: 30vh;
  }
  .markedCard {
    transform: translateY(-60px);
    box-shadow: 0px 8px 20px rgba(0,0,0,0.4);
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
