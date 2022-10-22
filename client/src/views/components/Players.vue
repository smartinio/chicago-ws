<template>
  <section class="section">
    <div class="container">
      <div class="columns">
        <div class="column" v-for="(player, index) in players">
          <div class="card">
            <div class="card-content">
              <Player
                :isMe="false"
                :fallbackName="'Player ' + index + 1"
                :player="player"
                :currentPlayer="currentPlayer"
                :baseMove="baseMove"
                :dealer="dealer"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>
<script>
import Player from '@/views/components/Player'
export default {
  name: 'Players',
  props: ['players', 'baseMove', 'currentPlayer', 'dealer'],
  components: {
    Player
  }
}
</script>
<style lang="scss" scoped>

</style>
