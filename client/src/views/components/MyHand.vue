<template>
  <section class="section">
    <div class="container">
      <div class="columns is-mobile">
        <div class="column has-text-centered" v-for="card in me.hand">
          <img
          @click="toggleMark(card)"
          :src="getCardUrl(card)"
          class="playingCard"
          :class="getCardClasses(card)">
        </div>
      </div>
    </div>
  </section>
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
    getCardClasses (card) {
      return !this.me.isMyTurn ? '' : this.isMarked(card) ? 'markedCard myTurn' : 'myTurn'
    }
  }
}
</script>
<style lang="scss" scoped>
</style>
