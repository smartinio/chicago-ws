<template>
  <div>
    <Start v-if="!game.invKey" @action="dispatchAction" @changeKey="clearKeyError" @changeNick="clearNickError"
      @rejoin="rejoin" @leave="leave" :connected="socket.connected" :errors="errors" :urlKey="$route.params.key"
      :currentlyInGame="game.currentlyInGame" />

    <Game v-else @rejoin="rejoin" @leave="leave" :connected="socket.connected" />
  </div>
</template>
<script>
import Start from './pages/Start.vue'
import Game from './pages/Game.vue'
import ConnectionStatus from './components/ConnectionStatus.vue'
import RejoinRequest from '@/dto/rejoinrequest/RejoinRequest'
import LeaveRequest from '@/dto/leaverequest/LeaveRequest'
import Action from '@/dto/action/Action'
import { RECONNECT, LEAVE_GAME } from '@/dto/action/types'
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
  watch: {
    'socket.status'(status) {
      if (status === 'failed') {
        setTimeout(() => this.connect(), 1000)
      }
    }
  },
  methods: {
    dispatchAction(actionDTO) {
      this.$store.dispatch(SEND_ACTION, actionDTO)
    },
    clearKeyError() {
      this.$store.commit(SET_KEY_ERROR, '')
    },
    clearNickError() {
      this.$store.commit(SET_NICKNAME_ERROR, '')
    },
    connect() {
      if (!this.socket.connected) {
        this.$store.dispatch(CONNECT)
      }
    },
    rejoin() {
      const storedInvitationKey = this.game.invKey || localStorage.getItem('invitationKey')
      const storedPlayerId = this.me.id || localStorage.getItem('playerId')

      if (!storedInvitationKey || !storedPlayerId) {
        alert('Sorry! Something went wrong... You won\'t be able to rejoin the game')
        this.leave()
        return
      }

      const request = new RejoinRequest(storedPlayerId, storedInvitationKey)
      const actionDTO = new Action(RECONNECT, request)
      this.dispatchAction(actionDTO)
    },
    leave() {
      const storedInvitationKey = this.game.invKey || localStorage.getItem('invitationKey')
      const storedPlayerId = this.me.id || localStorage.getItem('playerId')

      if (storedInvitationKey && storedPlayerId) {
        const request = new LeaveRequest(storedPlayerId, storedInvitationKey)
        const actionDTO = new Action(LEAVE_GAME, request)
        this.dispatchAction(actionDTO)
      }

      localStorage.removeItem('invitationKey')
      localStorage.removeItem('playerId')
    }
  },
  created() {
    this.connect()
  }
}
</script>
<style lang="scss" scoped>
</style>
