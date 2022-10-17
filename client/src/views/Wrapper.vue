<template>
  <div>
    <ConnectionStatus
    :connected="socket.connected"
    @reconnect="connect" />

    <Start
    @action="dispatchAction"
    @changeKey="clearKeyError"
    @changeNick="clearNickError"
    :errors="errors"
    :urlKey="$route.params.key"
    v-if="!game.invKey" />

    <Game
    :me="me"
    :game="game"
    v-else />
  </div>
</template>
<script>
import Start from './pages/Start'
import Game from './pages/Game'
import ConnectionStatus from './components/ConnectionStatus'
import { SOCKET, GAME, ME, ERRORS } from '@/store/state_mappings'
import { CONNECT, SEND_ACTION } from '@/store/modules/socket/action_types'
import { SET_KEY_ERROR, SET_NICKNAME_ERROR } from '@/store/modules/errors/mutation_types'
import { mapState } from 'vuex'
export default {
  components: {
    Start,
    Game,
    ConnectionStatus
  },
  name: 'Wrapper',
  computed: mapState({
    game: GAME,
    me: ME,
    errors: ERRORS,
    socket: SOCKET
  }),
  methods: {
    dispatchAction (actionDTO) {
      this.$store.dispatch(SEND_ACTION, actionDTO)
    },
    clearKeyError () {
      this.$store.commit(SET_KEY_ERROR, '')
    },
    clearNickError () {
      this.$store.commit(SET_NICKNAME_ERROR, '')
    },
    connect () {
      this.$store.dispatch(CONNECT)
    }
  },
  created () {
    this.connect()
  }
}
</script>
<style lang="scss" scoped>
</style>
