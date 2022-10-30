<template>
  <div style="transform: translateY(100px)" class="is-flex is-flex-direction-column is-justify-content-flex-end">
    <div :style="me.isMyTurn ? 'opacity: 1' : 'opacity: 0.4'" class="is-flex">
      <div
        v-for="card in me.hand"
        class="is-flex is-flex-direction-column is-align-items-center"
      >
        <a
          v-if="phase === 'PLAYING'"
          class="button is-small is-rounded is-success playingCard"
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
import { MOVE } from '@/dto/action/types'
import { SEND_ACTION } from '@/store/modules/socket/action_types'
import Action from '@/dto/action/Action'

const isCard = (a) => (b) => a.suit === b.suit && a.value === b.value

export default {
  props: ['me', 'phase', 'markedCards'],
  name: 'MyHand',
  methods: {
    toggleMark (card) {
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
    play (card) {
      if (!this.me.isMyTurn) return;
      const actionDTO = new Action(MOVE, card);
      this.$store.dispatch(SEND_ACTION, actionDTO)
      this.$emit('action')
    }
  }
}
</script>
<style lang="scss" scoped>
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
