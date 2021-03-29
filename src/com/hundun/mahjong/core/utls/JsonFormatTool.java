package com.hundun.mahjong.core.utls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.AbstractMap.SimpleEntry;

import org.mahjong4j.Player;
import org.mahjong4j.hands.Hands;
import org.mahjong4j.hands.Mentsu;
import org.mahjong4j.tile.Tile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hundun.mahjong.core.enhance.SuperHands;
import com.hundun.mahjong.core.game.DiscardAdvice;
import com.hundun.mahjong.core.game.GamePlayer;
import com.hundun.mahjong.core.game.TenpaiCase;
import com.hundun.mahjong.core.game.WinCase;
import com.hundun.mahjong.core.game.board.MahjongBoard;
import com.hundun.mahjong.core.game.board.TileRiver;
import com.hundun.mahjong.core.game.board.TileRiver.TileInRiverState;
import com.hundun.mahjong.core.game.event.GameEndEvent;

/**
 *
 * @author hundun
 * Created on 2019/03/09
 */
public class JsonFormatTool {
	
	public static String tileRiverToString(TileRiver tileRiver) {
		List<String> tileStrings = new ArrayList<>();
		for (SimpleEntry<Tile, TileInRiverState> entry : tileRiver.getTileWithStates()) {
		    switch (entry.getValue()) {
            case NORMAL:
                tileStrings.add(entry.getKey().toString());
                break;
            case NORMAL_REACH:
                tileStrings.add("*" + entry.getKey().toString());
                break;
            case MEI:
                tileStrings.add("-" + entry.getKey().toString());
                break;
            case MEI_REACH:
                tileStrings.add("*-" + entry.getKey().toString());
                break;
            }
			
		}
		return tileStrings.toString();
	}
	
	
	
	
	public static JSONObject handsToJson(SuperHands hands) {
		JSONObject handsJson = new JSONObject(true);
		String typeString;
		int stepMSP = 9;
		int stepKaze = 4;
		int stepSangen = 3;
		int from = 0;
		int to = 0;
		
		int[] inputtedTilesWithoutLast = hands.getInputtedTiles().clone();
		int[] tilesOfCurrentType;
//		if (hands.getLast() != null) {
//			inputtedTilesWithoutLast[hands.getLast().getCode()]--;
//		}
		
		
		from = to;
		to = to + stepMSP;
		tilesOfCurrentType = Arrays.copyOfRange(inputtedTilesWithoutLast, from, to);
		typeString = Tile.valueOf(from).getType().toString();
		handsJson.put(typeString, Arrays.toString(tilesOfCurrentType));
		
		from = to;
		to = to + stepMSP;
		tilesOfCurrentType = Arrays.copyOfRange(inputtedTilesWithoutLast, from, to);
		typeString = Tile.valueOf(from).getType().toString();
		handsJson.put(typeString, Arrays.toString(tilesOfCurrentType));
		
		from = to;
		to = to + stepMSP;
		tilesOfCurrentType = Arrays.copyOfRange(inputtedTilesWithoutLast, from, to);
		typeString = Tile.valueOf(from).getType().toString();
		handsJson.put(typeString, Arrays.toString(tilesOfCurrentType));
		
		from = to;
		to = to + stepKaze;
		tilesOfCurrentType = Arrays.copyOfRange(inputtedTilesWithoutLast, from, to);
		typeString = Tile.valueOf(from).getType().toString();
		handsJson.put(typeString, Arrays.toString(tilesOfCurrentType));
		
		from = to;
		to = to + stepSangen;
		tilesOfCurrentType = Arrays.copyOfRange(inputtedTilesWithoutLast, from, to);
		typeString = Tile.valueOf(from).getType().toString();
		handsJson.put(typeString, Arrays.toString(tilesOfCurrentType));
		
		// 添加last
		if (hands.getLast() != null) {
			handsJson.put("last", hands.getLast().toString());
		}
		// 添加副露的牌
		JSONArray inputtedMentsus = new JSONArray();
		for (Mentsu mentsu : hands.getInputtedMentsuList()) {
		    inputtedMentsus.add(mentsu.getTiles());
		}
		handsJson.put("inputted_mentsus", inputtedMentsus);
		
		
		return handsJson;
	}
	
	public static JSONObject mahjongBoardToJson(MahjongBoard board) {
	    JSONObject boardJson = new JSONObject();
	    boardJson.put("state_message", board.getStateMessage());
    	if (board.getGameEndEvent() != null) {
    	    boardJson.put("event", board.getGameEndEvent());
        }
    	JSONArray players = new JSONArray();
    	for (int i = 0; i < MahjongBoard.NUM_PLAYERS; i++) {
    		JSONObject playerObject = new JSONObject(true);
    		GamePlayer player = board.getPlayer(i);
    		
    		
    		if (player.getChiCandidates().size() > 0) {
    			playerObject.put("chi_candicates", meiPaiCandidatesToJSON(player.getChiCandidates()));
    		}
    		
    		if (player.getPonCandidates().size() > 0) {
                playerObject.put("chi_candicates", meiPaiCandidatesToJSON(player.getPonCandidates()));
            }
    		
    		JSONArray discardAdvices = discardAdvicesToJSON(player.getDiscardAdvices());
    		if (discardAdvices.size() > 0) {
                playerObject.put("discard_advices", discardAdvices);
                playerObject.put("can_reach", player.isCanReach());
            }
    		
    		JSONArray tenpaiCases = tenpaiCasesToJSON(player.getTenpaiCases());
            if (tenpaiCases.size() > 0) {
                playerObject.put("tenpai", tenpaiCases);
                playerObject.put("huriten", player.isHuriten());
            }
    		
    		JSONObject wincase = wincaseToJSON(player.getWinCase());
            if (wincase != null) {
                playerObject.put("wincase", wincase);
            }
            
            playerObject.put("action_advices", player.getActionAdvices());
    		
            boolean isReach = player.getPersonalSituation().isReach();
            if (isReach) {
                playerObject.put("reach", true);
            }
    		playerObject.put("hands", handsToJson(player.getHands()));
    		playerObject.put("river", tileRiverToString(player.getTileRiver()));
    		players.add(playerObject);
    	}
    	boardJson.put("players", players);
    	return boardJson;
    }
	
	private static JSONArray tenpaiCasesToJSON(List<TenpaiCase> tenpaiCases) {
	    JSONArray result = new JSONArray();
        for (TenpaiCase tenpaiCase : tenpaiCases) {
            JSONObject object = new JSONObject();
            object.put("discard", tenpaiCase.getTile().toString());
            // TODO
            object.put("info", tenpaiCase.yakuInfoToString(false));
            result.add(object);
        }
        return result;
    }

    private static JSONObject wincaseToJSON(WinCase winCase) {
	    if (winCase == null) {
	        return null;
	    }
	    
	    JSONObject object = new JSONObject();
	    // TODO
	    object.put("info", CharImageTool.WinCaseToCharImage(winCase));
        return object;
    }




    private static JSONArray discardAdvicesToJSON(List<DiscardAdvice> discardAdvices) {
	    JSONArray result = new JSONArray();
	    for (DiscardAdvice discardAdvice : discardAdvices) {
            JSONObject object = new JSONObject();
            object.put("furiten", discardAdvice.isFuriten());
            object.put("discard", discardAdvice.getDiscardTile().toString());
            object.put("tenpai", discardAdvice.getTenpaiList().toString());
            result.add(object);
        }
        return result;
    }




    public static JSONArray meiPaiCandidatesToJSON(List<? extends Mentsu> meiPaiCandidates) {
		JSONArray array = new JSONArray();
        for (Mentsu mentsu : meiPaiCandidates) {
            array.add(mentsu.getTiles());
        }
        return array;
	}
	
}
