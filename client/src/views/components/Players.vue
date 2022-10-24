<template>
    <div class="is-flex is-flex-direction-column" style="width: 100%">
      <div v-for="(player, index) in players" style="padding-top: 15px">
        <Player
          :isMe="false"
          variant="large"
          :fallbackName="'Player ' + index + 1"
          :player="player"
          :currentPlayer="currentPlayer"
          :baseMove="baseMove"
          :dealer="dealer"
        />
      </div>
    </div>
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
