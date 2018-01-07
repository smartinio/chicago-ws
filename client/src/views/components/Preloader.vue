<template>
  <div style="visibility: hidden; height: 0; width: 0">
    <div v-for="suit in cards.suits">
      <img v-for="value in cards.values" :src="getSrc(value, suit)">
    </div>
  </div>
</template>

<script>
export default {
  methods: {
    getSrc (value, suit) {
      return this.getCardUrl({value, suit})
    }
  },
  data () {
    return {
      cards: {
        suits: {
          HEARTS: 'HEARTS',
          SPADES: 'SPADES',
          DIAMONDS: 'DIAMONDS',
          CLUBS: 'CLUBS'
        },
        values: {
          TWO: 'TWO',
          THREE: 'THREE',
          FOUR: 'FOUR',
          FIVE: 'FIVE',
          SIX: 'SIX',
          SEVEN: 'SEVEN',
          EIGHT: 'EIGHT',
          NINE: 'NINE',
          TEN: 'TEN',
          JACK: 'JACK',
          QUEEN: 'QUEEN',
          KING: 'KING',
          ACE: 'ACE'
        }
      }
    }
  }
}
</script>
