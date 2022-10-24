<template>
  <div style="transform: translateY(100px)" class="is-flex is-flex-direction-column is-justify-content-flex-end">
    <div :style="me.isMyTurn ? 'opacity: 1' : 'opacity: 0.4'" class="is-flex">
      <div
        v-for="card in me.hand"
      >
        <img
        @click="toggleMark(card)"
        :src="getCardUrl(card)"
        class="playingCard"
        :class="getCardClass(card)"
        />
      </div>
    </div>
  </div>
</template>
<script>
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
</style>
