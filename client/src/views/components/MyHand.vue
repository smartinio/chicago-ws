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
export default {
  props: ['me', 'phase', 'markedCards'],
  name: 'MyHand',
  methods: {
    toggleMark (card) {
      this.$emit('toggleMark', card)
    },
    isMarked (card) {
      return this.markedCards.includes(card)
    },
    getCardClasses (card) {
      return !this.me.isMyTurn ? '' : this.isMarked(card) ? 'markedCard myTurn' : 'myTurn'
    }
  }
}
</script>
<style lang="scss" scoped>
</style>
