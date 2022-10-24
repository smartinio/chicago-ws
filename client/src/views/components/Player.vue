<template>
  <div class="media">
    <div class="media-left">
      <figure class="image" :class="figureClass">
        <img
          :src="getAvatarUrl(player)"
          alt="Placeholder image"
          class="avatar"
        >
      </figure>
    </div>
    <div class="media-content" style="overflow: hidden">
      <div class="columns">
        <div class="column is-narrow">
          <p class="title is-4">
            {{ player.name || fallbackName }}
          </p>
          <p class="subtitle is-6">
            {{ player.score }} points
          </p>
        </div>
        <div class="column is-expanded">
          <div v-if="player.hand" style="padding-right: 40px">
            <figure
              v-for="card in player.hand.played"
              class="image is-inline-flex"
              style="width: 66px; height: 96px; margin-right: -40px"
            >
              <img
                :src="getCardUrl(card)"
                :alt="card.type + '' + card.value"
                :class="isBaseCard(card) ? 'baseCard' : ''"
              >
            </figure>
          </div>
        </div>
        <div class="column is-narrow" v-if="!isMe">
          <PlayerStatus
            :currentPlayer="currentPlayer"
            :dealer="dealer"
            :player="player"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import PlayerStatus from '@/views/components/PlayerStatus'

export default {
  name: 'Player',
  props: ['player', 'baseMove', 'currentPlayer', 'dealer', 'fallbackName', 'variant', 'isMe'],
  components: {
    PlayerStatus
  },
  computed: {
    figureClass() {
      const size = this.variant === 'large' ? 96 : 48
      return `is-${size}x${size}`
    }
  },
  methods: {
    isBaseCard (card) {
      if (!this.baseMove) return false
      return (card.suit === this.baseMove.card.suit && card.value === this.baseMove.card.value)
    },
  }
}
</script>
