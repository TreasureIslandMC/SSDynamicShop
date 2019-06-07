package me.sat7.dynamicshop.Events;

import me.sat7.dynamicshop.DynaShopAPI;
import me.sat7.dynamicshop.DynamicShop;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.*;

public class OnClick implements Listener {

    DecimalFormat df = new DecimalFormat("0.00");

    // UI 인벤토리에 드래그로 아이탬 올리는것을 막음
    @EventHandler
    public void onDragInGUI(InventoryDragEvent event) {
        if (event.getInventory() != null && DynamicShop.ccShop.get().contains(ChatColor.stripColor(event.getView().getTitle()))) {
            if(event.getInventory().getSize() == 54 && event.getInventory().getItem(53).getItemMeta().getDisplayName().
                    equals(DynamicShop.ccLang.get().getString("SHOP_INFO")))
            {
                event.setCancelled(true);
            }
        }
        else if (event.getInventory() != null && event.getView().getTitle().equalsIgnoreCase(DynamicShop.ccLang.get().getString("TRADE_TITLE"))) { event.setCancelled(true); }
        else if (event.getInventory() != null && event.getView().getTitle().equalsIgnoreCase(DynamicShop.ccLang.get().getString("PALETTE_TITLE"))) { event.setCancelled(true); }
        else if (event.getInventory() != null && event.getView().getTitle().equalsIgnoreCase(DynamicShop.ccLang.get().getString("SHOP_SETTING_TITLE"))) { event.setCancelled(true); }
        else if (event.getInventory() != null && event.getView().getTitle().equalsIgnoreCase(DynamicShop.ccLang.get().getString("ITEM_SETTING_TITLE"))) { event.setCancelled(true); }
        else if (event.getInventory() != null && event.getView().getTitle().equalsIgnoreCase(DynamicShop.ccStartpage.get().getString("Options.Title"))) { event.setCancelled(true); }
        else if (event.getInventory() != null && event.getView().getTitle().equalsIgnoreCase(DynamicShop.ccLang.get().getString("QUICKSELL_TITLE"))) { event.setCancelled(true); }
        else if (event.getInventory() != null && event.getView().getTitle().equalsIgnoreCase(DynamicShop.ccLang.get().getString("START.PAGEEDITOR_TITLE"))) { event.setCancelled(true); }
    }

    // 인벤토리 클릭
    @EventHandler
    public void OnInvenClick(InventoryClickEvent e)
    {
        if(e.getClickedInventory() == null)
        {
            return;
        }

        Player player = (Player)e.getWhoClicked();

        // 클릭된 인벤토리가 UI임
        if(e.getClickedInventory() != player.getInventory())
        {
            // UUID 확인
            String pUuid = player.getUniqueId().toString();

            if(DynamicShop.ccUser.get().getConfigurationSection(pUuid)==null)
            {
                if(!DynaShopAPI.RecreateUserData(player))
                {
                    player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("ERR.NO_USER_ID"));
                    e.setCancelled(true);
                    return;
                }
            }

            // 스타트페이지
            if(e.getView().getTitle().equalsIgnoreCase(DynamicShop.ccStartpage.get().getString("Options.Title")))
            {
                e.setCancelled(true);
                DynaShopAPI.PlayerSoundEffect(player,"click");

                if(e.isLeftClick())
                {
                    if(e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
                    {
                        // 새 버튼 추가
                        if(player.hasPermission("dshop.admin.shopedit"))
                        {
                            DynamicShop.ccStartpage.get().set("Buttons." + e.getSlot() + ".displayName", "New Button");
                            DynamicShop.ccStartpage.get().set("Buttons." + e.getSlot() + ".lore", "new button");
                            DynamicShop.ccStartpage.get().set("Buttons." + e.getSlot() + ".icon", Material.SUNFLOWER.name());
                            DynamicShop.ccStartpage.get().set("Buttons." + e.getSlot() + ".action", "ds");
                            DynamicShop.ccStartpage.save();

                            DynaShopAPI.OpenStartPage(player);
                        }
                        else
                        {
                            return;
                        }
                    }

                    String actionStr = DynamicShop.ccStartpage.get().getString("Buttons."+e.getSlot()+".action");
                    if(actionStr.length() > 0)
                    {
                        String[] action = actionStr.split(DynamicShop.ccStartpage.get().getString("Options.LineBreak"));

                        //player.closeInventory();

                        for (String s:action) {
                            Bukkit.dispatchCommand(player, s);
                        }
                    }
                }
                // 우클릭
                else if(player.hasPermission("dshop.admin.shopedit"))
                {
                    // 편집
                    if(e.isShiftClick())
                    {
                        if(e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

                        DynamicShop.ccUser.get().set(player.getUniqueId()+".interactItem","startpage/" + e.getSlot()); // 선택한 아이탬의 인덱스 저장
                        DynaShopAPI.OpenStartpageSettingGUI(player, e.getSlot());
                    }
                    // 이동
                    else
                    {
                        String itemtoMove = "";
                        try
                        {
                            String[] temp = DynamicShop.ccUser.get().getString(player.getUniqueId()+".interactItem").split("/");
                            itemtoMove = temp[1];
                        }
                        catch (Exception exception)
                        {

                        }

                        if(itemtoMove.length() == 0)
                        {
                            if(e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

                            DynamicShop.ccUser.get().set(player.getUniqueId()+".interactItem","startpage/" + e.getSlot()); // 선택한 아이탬의 인덱스 저장
                            player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("ITEM_MOVE_SELECTED"));
                        }
                        else
                        {
                            if(e.getCurrentItem().getType() != Material.AIR) return;

                            DynamicShop.ccStartpage.get().set("Buttons." + e.getSlot() + ".displayName", DynamicShop.ccStartpage.get().get("Buttons." +itemtoMove+".displayName"));
                            DynamicShop.ccStartpage.get().set("Buttons." + e.getSlot() + ".lore", DynamicShop.ccStartpage.get().get("Buttons." +itemtoMove+".lore"));
                            DynamicShop.ccStartpage.get().set("Buttons." + e.getSlot() + ".icon", DynamicShop.ccStartpage.get().get("Buttons." +itemtoMove+".icon"));
                            DynamicShop.ccStartpage.get().set("Buttons." + e.getSlot() + ".action", DynamicShop.ccStartpage.get().get("Buttons." +itemtoMove+".action"));

                            if(DynamicShop.ccStartpage.get().getString("Buttons."+itemtoMove+".action").length() > 0)
                            {
                                DynamicShop.ccStartpage.get().set("Buttons." + itemtoMove,null);
                            }

                            DynamicShop.ccStartpage.save();

                            DynaShopAPI.OpenStartPage(player);
                            DynamicShop.ccUser.get().set(player.getUniqueId()+".interactItem","");
                        }
                    }
                }

                return;
            }
            // 스타트페이지 편집
            else if(e.getView().getTitle().equalsIgnoreCase(DynamicShop.ccLang.get().getString("STARTPAGE.EDITOR_TITLE")))
            {
                e.setCancelled(true);
                DynaShopAPI.PlayerSoundEffect(player,"click");

                String[] temp = DynamicShop.ccUser.get().getString(player.getUniqueId()+".interactItem").split("/");

                // 돌아가기
                if(e.getSlot() == 0)
                {
                    DynaShopAPI.OpenStartPage(player);
                }
                // 버튼 삭제
                else if(e.getSlot() == 8)
                {
                    DynamicShop.ccStartpage.get().set("Buttons." + temp[1],null);
                    DynamicShop.ccStartpage.save();

                    DynamicShop.ccUser.get().set(player.getUniqueId()+".interactItem","");

                    DynaShopAPI.OpenStartPage(player);
                }
                //이름
                else if(e.getSlot() == 2)
                {
                    player.sendMessage(DynamicShop.dsPrefix+DynamicShop.ccLang.get().getString("STARTPAGE.ENTER_NAME"));
                    DynaShopAPI.CloseInventoryWithDelay(player);
                    DynamicShop.ccUser.get().set(player.getUniqueId()+".tmpString","waitforInput"+"btnName");
                    OnChat.WaitForInput(player);
                }
                //설명
                else if(e.getSlot() == 3)
                {
                    player.sendMessage(DynamicShop.dsPrefix+DynamicShop.ccLang.get().getString("STARTPAGE.ENTER_LORE"));
                    DynaShopAPI.CloseInventoryWithDelay(player);
                    DynamicShop.ccUser.get().set(player.getUniqueId()+".tmpString","waitforInput"+"btnLore");
                    OnChat.WaitForInput(player);
                }
                //아이콘
                else if(e.getSlot() == 4)
                {
                    player.sendMessage(DynamicShop.dsPrefix+DynamicShop.ccLang.get().getString("STARTPAGE.ENTER_ICON"));
                    DynaShopAPI.CloseInventoryWithDelay(player);
                    DynamicShop.ccUser.get().set(player.getUniqueId()+".tmpString","waitforInput"+"btnIcon");
                    OnChat.WaitForInput(player);
                }
                //액션
                else if(e.getSlot() == 5)
                {
                    player.sendMessage(DynamicShop.dsPrefix+DynamicShop.ccLang.get().getString("STARTPAGE.ENTER_ACTION"));
                    DynaShopAPI.CloseInventoryWithDelay(player);
                    DynamicShop.ccUser.get().set(player.getUniqueId()+".tmpString","waitforInput"+"btnAction");
                    OnChat.WaitForInput(player);
                }
                // 상점 숏컷
                else if (e.getSlot()==6)
                {
                    player.sendMessage(DynamicShop.dsPrefix+DynamicShop.ccLang.get().getString("STARTPAGE.ENTER_SHOPNAME"));
                    DynaShopAPI.CloseInventoryWithDelay(player);
                    DynamicShop.ccUser.get().set(player.getUniqueId()+".tmpString","waitforInput"+"shopname");
                    OnChat.WaitForInput(player);
                }
                // 장식
                else if(e.getSlot() == 7)
                {
                    player.sendMessage(DynamicShop.dsPrefix+DynamicShop.ccLang.get().getString("STARTPAGE.ENTER_COLOR"));
                    DynaShopAPI.CloseInventoryWithDelay(player);
                    DynamicShop.ccUser.get().set(player.getUniqueId()+".tmpString","waitforInput"+"deco");
                    OnChat.WaitForInput(player);
                }
            }
            // 상점
            else if(DynamicShop.ccShop.get().contains(ChatColor.stripColor(e.getView().getTitle())))
            {
                if(e.getInventory().getSize() == 54 && e.getInventory().getItem(53).getItemMeta().getDisplayName().
                        equals(DynamicShop.ccLang.get().getString("SHOP_INFO")))
                {
                    e.setCancelled(true);
                }
                else
                {
                    return;
                }

                String shopName = ChatColor.stripColor(e.getView().getTitle());
                int curPage = e.getInventory().getItem(49).getAmount();

                String itemtoMove = "";
                if(DynamicShop.ccUser.get().contains(player.getUniqueId()+".interactItem"))
                {
                    String[] temp = DynamicShop.ccUser.get().getString(player.getUniqueId()+".interactItem").split("/");
                    if(temp.length>1) itemtoMove = temp[1];
                }

                // 빈칸이 아닌 뭔가 클릭함
                if(e.getCurrentItem() != null && e.getCurrentItem().getType() != Material.AIR)
                {
                    // 닫기버튼
                    if(e.getSlot() == 45)
                    {
                        DynaShopAPI.PlayerSoundEffect(player,"click");
                        DynaShopAPI.OpenStartPage(player);
                    }
                    // 페이지 이동 버튼
                    else if(e.getSlot() == 49)
                    {
                        DynaShopAPI.PlayerSoundEffect(player,"click");

                        int targetPage = curPage;
                        if(e.isLeftClick())
                        {
                            if(!e.isShiftClick())
                            {
                                targetPage -= 1;
                                if(targetPage<1)targetPage = DynamicShop.ccShop.get().getConfigurationSection(shopName).getConfigurationSection("Options").getInt("page");
                            }
                            else if(player.hasPermission("dshop.admin.shopedit"))
                            {
                                DynaShopAPI.InsetShopPage(shopName,curPage);
                            }
                        }
                        else if(e.isRightClick())
                        {
                            if(!e.isShiftClick())
                            {
                                targetPage += 1;
                                if(targetPage>DynamicShop.ccShop.get().getConfigurationSection(shopName).getConfigurationSection("Options").getInt("page"))targetPage = 1;
                            }
                            else if(player.hasPermission("dshop.admin.shopedit"))
                            {
                                if(DynamicShop.ccShop.get().getInt(shopName+".Options.page") > 1)
                                {
                                    DynaShopAPI.CloseInventoryWithDelay(player);

                                    DynamicShop.ccUser.get().set(player.getUniqueId()+".interactItem",shopName+"/"+curPage);
                                    DynamicShop.ccUser.get().set(player.getUniqueId()+".tmpString","waitforPageDelete");
                                    OnChat.WaitForInput(player);

                                    player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("RUSURE"));
                                }
                                else
                                {
                                    player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("CANT_DELETE_LAST_PAGE"));
                                }

                                return;
                            }
                        }
                        DynaShopAPI.OpenShopGUI(player,shopName,targetPage);
                    }
                    // 상점 설정 버튼
                    else if(e.getSlot() == 53 && e.isRightClick() && player.hasPermission("dshop.admin.shopedit"))
                    {
                        DynaShopAPI.PlayerSoundEffect(player,"click");
                        DynamicShop.ccUser.get().set(player.getUniqueId()+".interactItem",shopName + "/" + 0); // 선택한 아이탬의 인덱스 저장
                        DynaShopAPI.OpenShopSettingUI(player, shopName);
                        return;
                    }
                    else if(e.getSlot()>45)
                    {
                        return;
                    }
                    // 상점의 아이탬 클릭
                    else
                    {
                        int idx = e.getSlot() + (45 * (curPage - 1));
                        String mat = DynamicShop.ccShop.get().getString(shopName+"."+idx+".mat");

                        // 거래화면 열기
                        if(e.isLeftClick())
                        {
                            if(!DynamicShop.ccShop.get().contains(shopName+"."+idx+".value")) return; // 장식용 버튼임

                            DynaShopAPI.PlayerSoundEffect(player,"tradeview");
                            DynaShopAPI.OpenItemTradeInven(player,shopName, String.valueOf(idx));
                            DynamicShop.ccUser.get().set(player.getUniqueId()+".interactItem",shopName + "/" + idx); // 선택한 아이탬의 인덱스 저장
                        }
                        // 아이탬 이동, 수정, 또는 장식탬 삭제
                        else if(player.hasPermission("dshop.admin.editshop"))
                        {
                            DynaShopAPI.PlayerSoundEffect(player,"click");

                            DynamicShop.ccUser.get().set(player.getUniqueId()+".interactItem",shopName + "/" + idx); // 선택한 아이탬의 인덱스 저장
                            if(e.isShiftClick())
                            {
                                if(DynamicShop.ccShop.get().contains(shopName+"."+idx+".value"))
                                {
                                    double value = DynamicShop.ccShop.get().getDouble(shopName+"."+idx+".value");
                                    double valueMin = DynamicShop.ccShop.get().getDouble(shopName+"."+idx+".valueMin");
                                    if(valueMin <= 0.01) valueMin = 0.01;
                                    double valueMax = DynamicShop.ccShop.get().getDouble(shopName+"."+idx+".valueMax");
                                    if(valueMax <= 0) valueMax = -1;
                                    int median = DynamicShop.ccShop.get().getInt(shopName+"."+idx+".median");
                                    int stock = DynamicShop.ccShop.get().getInt(shopName+"."+idx+".stock");

                                    ItemStack iStack = new ItemStack(e.getCurrentItem().getType());
                                    iStack.setItemMeta((ItemMeta) DynamicShop.ccShop.get().get(shopName + "." + idx + ".itemStack"));

                                    DynaShopAPI.OpenItemSettingGUI(player,iStack,1,value,valueMin,valueMax,median,stock);
                                }
                                else
                                {
                                    DynaShopAPI.RemoveItemFromShop(shopName,idx);
                                    DynaShopAPI.OpenShopGUI(player,shopName,idx/45+1);
                                }
                            }
                            else if(itemtoMove.equals(""))
                            {
                                player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("ITEM_MOVE_SELECTED"));
                            }
                        }
                    }
                }
                // 빈칸 클릭함
                else
                {
                    if(e.getSlot()>45)
                    {
                        return;
                    }

                    int clickedIdx = e.getSlot() + ((curPage-1)*45);

                    // 아이탬 이동. 또는 장식 복사
                    if(e.isRightClick() && player.hasPermission("dshop.admin.editshop") && !itemtoMove.equals(""))
                    {
                        DynamicShop.ccShop.get().set(shopName+"." + clickedIdx + ".mat",DynamicShop.ccShop.get().get(shopName+"."+itemtoMove+".mat"));
                        DynamicShop.ccShop.get().set(shopName+"." + clickedIdx + ".itemStack",DynamicShop.ccShop.get().get(shopName+"."+itemtoMove+".itemStack"));
                        DynamicShop.ccShop.get().set(shopName+"." + clickedIdx + ".value",DynamicShop.ccShop.get().get(shopName+"."+itemtoMove+".value"));
                        DynamicShop.ccShop.get().set(shopName+"." + clickedIdx + ".median",DynamicShop.ccShop.get().get(shopName+"."+itemtoMove+".median"));
                        DynamicShop.ccShop.get().set(shopName+"." + clickedIdx + ".stock",DynamicShop.ccShop.get().get(shopName+"."+itemtoMove+".stock"));

                        if(DynamicShop.ccShop.get().contains(shopName+"."+itemtoMove+".value"))
                        {
                            DynamicShop.ccShop.get().set(shopName+"."+itemtoMove,null);
                        }

                        DynamicShop.ccShop.save();

                        DynaShopAPI.OpenShopGUI(player,shopName,curPage);
                        DynamicShop.ccUser.get().set(player.getUniqueId()+".interactItem","");
                    }
                    // 팔렛트 열기
                    else if(player.hasPermission("dshop.admin.editshop"))
                    {
                        DynamicShop.ccUser.get().set(player.getUniqueId()+".interactItem",shopName + "/" + clickedIdx); // 선택한 아이탬의 인덱스 저장
                        DynaShopAPI.OpenItemPalette(player,1,"");
                    }
                }
            }
            // 상점설정
            else if(e.getView().getTitle().equalsIgnoreCase(DynamicShop.ccLang.get().getString("SHOP_SETTING_TITLE")))
            {
                e.setCancelled(true);
                DynaShopAPI.PlayerSoundEffect(player,"click");

                String[] temp = DynamicShop.ccUser.get().getString(player.getUniqueId()+".interactItem").split("/");
                String shopName = temp[0];

                // 닫기버튼
                if(e.getSlot() == 27)
                {
                    DynaShopAPI.OpenShopGUI(player,temp[0],1);
                    DynamicShop.ccUser.get().set(player.getUniqueId()+".interactItem","");
                }
                // 권한
                else if(e.getSlot() == 0)
                {
                    if(DynamicShop.ccShop.get().getString(shopName+".Options.permission").isEmpty())
                    {
                        Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" permission true");
                    }
                    else
                    {
                        Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" permission false");
                    }
                    DynaShopAPI.OpenShopSettingUI(player,shopName);
                }
                // 최대 페이지
                else if(e.getSlot() == 1)
                {
                    int oldvalue = DynamicShop.ccShop.get().getInt(shopName+".Options.page");
                    int targetValue;

                    if(e.isRightClick())
                    {
                        targetValue = oldvalue + 1;
                        if(e.isShiftClick()) targetValue += 4;
                        if(targetValue>=20) targetValue = 20;
                        Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" maxpage " + targetValue);
                    }
                    else
                    {
                        targetValue = oldvalue - 1;
                        if(e.isShiftClick()) targetValue -= 4;
                        if(targetValue<=1) targetValue = 1;
                        Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" maxpage " + targetValue);
                    }
                    DynaShopAPI.OpenShopSettingUI(player,shopName);
                }
                // 영업시간
                else if(e.getSlot()>=6 && e.getSlot()<=8)
                {
                    if(DynamicShop.ccShop.get().contains(shopName+".Options.shophours"))
                    {
                        String[] shopHour = DynamicShop.ccShop.get().getString(shopName+".Options.shophours").split("~");
                        Integer open = Integer.parseInt(shopHour[0]);
                        int close = Integer.parseInt(shopHour[1]);
                        int edit = -1;
                        if(e.isRightClick()) edit = 1;
                        if(e.isShiftClick()) edit *= 5;

                        if(e.getSlot()==6)
                        {
                            Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" shophours 0 0");
                        }
                        else if(e.getSlot() == 7)
                        {
                            open += edit;

                            if(open.equals(close))
                            {
                                if(e.isRightClick())
                                {
                                    open += 1;
                                }
                                else
                                {
                                    open -= 1;
                                }
                            }

                            if(open < 1) open = 1;
                            if(open > 24) open = 24;

                            Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" shophours "+open+" "+close);
                        }
                        else if(e.getSlot() == 8)
                        {
                            close += edit;

                            if(open.equals(close))
                            {
                                if(e.isRightClick())
                                {
                                    close += 1;
                                }
                                else
                                {
                                    close -= 1;
                                }
                            }

                            if(close < 1) close = 1;
                            if(close > 24) close = 24;

                            Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" shophours "+open+" "+close);
                        }

                        DynaShopAPI.OpenShopSettingUI(player,shopName);
                    }
                    else
                    {
                        if(e.getSlot()==6)
                        {
                            Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" shophours 20 6");
                            DynaShopAPI.OpenShopSettingUI(player,shopName);
                        }
                    }
                }
                // 랜덤스톡
                else if(e.getSlot()>=15 && e.getSlot()<=17)
                {
                    if(DynamicShop.ccShop.get().contains(shopName+".Options.fluctuation"))
                    {
                        Integer interval = DynamicShop.ccShop.get().getInt(shopName+".Options.fluctuation.interval");
                        int strength = DynamicShop.ccShop.get().getInt(shopName+".Options.fluctuation.strength");
                        ArrayList<Integer> intervalOptions = new ArrayList<>();
                        intervalOptions.add(1);
                        intervalOptions.add(2);
                        intervalOptions.add(4);
                        intervalOptions.add(8);
                        intervalOptions.add(24);
                        int intervalIdx = intervalOptions.indexOf(interval);

                        ArrayList<String> intervalArg = new ArrayList<>();
                        intervalArg.add("30m");
                        intervalArg.add("1h");
                        intervalArg.add("2h");
                        intervalArg.add("4h");
                        intervalArg.add("12h");

                        int edit = -1;
                        if(e.isRightClick()) edit = 1;
                        if(e.isShiftClick()) edit *= 5;

                        if(e.getSlot()==15)
                        {
                            Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" fluctuation off");
                        }
                        else if(e.getSlot() == 16)
                        {
                            intervalIdx += edit;

                            if(intervalIdx < 0) intervalIdx = 0;

                            if(intervalIdx > 4)
                            {
                                intervalIdx = 4;
                            }


                            Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" fluctuation "+intervalArg.get(intervalIdx)+" "+strength);
                        }
                        else if(e.getSlot() == 17)
                        {
                            strength += edit;

                            if(strength < 1) strength = 1;
                            if(strength > 60) strength = 60;

                            Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" fluctuation "+intervalArg.get(intervalIdx)+" "+strength);
                        }

                        DynaShopAPI.OpenShopSettingUI(player,shopName);
                    }
                    else
                    {
                        if(e.getSlot()==15)
                        {
                            Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" fluctuation 1h 1");
                            DynaShopAPI.OpenShopSettingUI(player,shopName);
                        }
                    }
                }
                // 스톡 안정화
                else if(e.getSlot()>=24 && e.getSlot()<=26)
                {
                    if(DynamicShop.ccShop.get().contains(shopName+".Options.stockStabilizing"))
                    {
                        Integer interval = DynamicShop.ccShop.get().getInt(shopName+".Options.stockStabilizing.interval");
                        double strength = DynamicShop.ccShop.get().getDouble(shopName+".Options.stockStabilizing.strength");
                        ArrayList<Integer> intervalOptions = new ArrayList<>();
                        intervalOptions.add(1);
                        intervalOptions.add(2);
                        intervalOptions.add(4);
                        intervalOptions.add(8);
                        intervalOptions.add(24);
                        int intervalIdx = intervalOptions.indexOf(interval);

                        ArrayList<String> intervalArg = new ArrayList<>();
                        intervalArg.add("30m");
                        intervalArg.add("1h");
                        intervalArg.add("2h");
                        intervalArg.add("4h");
                        intervalArg.add("12h");

                        if(e.getSlot()==24)
                        {
                            Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" stockStabilizing off");
                        }
                        else if(e.getSlot() == 25)
                        {
                            double edit = -1;
                            if(e.isRightClick()) edit = 1;
                            if(e.isShiftClick()) edit *= 5;

                            intervalIdx += edit;

                            if(intervalIdx < 0) intervalIdx = 0;

                            if(intervalIdx > 4)
                            {
                                intervalIdx = 4;
                            }

                            Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" stockStabilizing "+intervalArg.get(intervalIdx)+" "+strength);
                        }
                        else if(e.getSlot() == 26)
                        {
                            double edit = -0.1;
                            if(e.isRightClick()) edit = 0.1;
                            if(e.isShiftClick()) edit *= 5;

                            strength += edit;

                            if(strength < 0.1) strength = 0.1;
                            if(strength > 25) strength = 25;

                            strength = (Math.round(strength*100)/100.0);

                            Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" stockStabilizing "+intervalArg.get(intervalIdx)+" "+strength);
                        }

                        DynaShopAPI.OpenShopSettingUI(player,shopName);
                    }
                    else
                    {
                        if(e.getSlot()==24)
                        {
                            Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" stockStabilizing 12h 0.5");
                            DynaShopAPI.OpenShopSettingUI(player,shopName);
                        }
                    }
                }
                // 세금
                else if(e.getSlot() == 33 || e.getSlot() == 34)
                {
                    // 전역,지역 토글
                    if(e.getSlot() == 33)
                    {
                        if(DynamicShop.ccShop.get().contains(shopName+".Options.SalesTax"))
                        {
                            DynamicShop.ccShop.get().set(shopName + ".Options.SalesTax", null);
                        }
                        else
                        {
                            DynamicShop.ccShop.get().set(shopName + ".Options.SalesTax", DynamicShop.plugin.getConfig().getInt("SalesTax"));
                        }

                        DynaShopAPI.OpenShopSettingUI(player,shopName);
                    }
                    // 수치설정
                    else if(DynamicShop.ccShop.get().contains(shopName+".Options.SalesTax"))
                    {
                        int edit = -1;
                        if(e.isRightClick()) edit = 1;
                        if(e.isShiftClick()) edit *= 5;

                        int result = DynamicShop.ccShop.get().getInt(shopName+".Options.SalesTax") + edit;
                        if(result < 2) result = 2;
                        if(result > 99) result = 99;

                        DynamicShop.ccShop.get().set(shopName + ".Options.SalesTax", result);

                        DynaShopAPI.OpenShopSettingUI(player,shopName);
                    }
                    DynamicShop.ccShop.save();
                }
                // signshop
                else if(e.getSlot() == 9)
                {
                    if(DynamicShop.ccShop.get().contains(shopName+".Options.flag.signshop"))
                    {
                        Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" flag signshop unset");
                    }
                    else
                    {
                        Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" flag signshop set");
                    }
                    DynaShopAPI.OpenShopSettingUI(player,shopName);
                }
                // localshop
                else if(e.getSlot() == 10)
                {
                    if(DynamicShop.ccShop.get().contains(shopName+".Options.flag.localshop"))
                    {
                        Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" flag localshop unset");
                    }
                    else
                    {
                        Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" flag localshop set");
                    }
                    DynaShopAPI.OpenShopSettingUI(player,shopName);
                }
                // deliverycharge
                else if(e.getSlot() == 11)
                {
                    if(DynamicShop.ccShop.get().contains(shopName+".Options.flag.deliverycharge"))
                    {
                        Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" flag deliverycharge unset");
                    }
                    else
                    {
                        Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" flag deliverycharge set");
                    }
                    DynaShopAPI.OpenShopSettingUI(player,shopName);
                }
                // jobpoint
                else if(e.getSlot() == 12)
                {
                    if(DynamicShop.ccShop.get().contains(shopName+".Options.flag.jobpoint"))
                    {
                        Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" flag jobpoint unset");
                    }
                    else
                    {
                        if(!DynamicShop.jobsRebornActive)
                        {
                            player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("ERR.JOBSREBORN_NOT_FOUND"));
                            return;
                        }

                        Bukkit.dispatchCommand(player, "DynamicShop shop "+shopName+" flag jobpoint set");
                    }
                    DynaShopAPI.OpenShopSettingUI(player,shopName);
                }
            }
            // 거래화면
            else if(e.getView().getTitle().equalsIgnoreCase(DynamicShop.ccLang.get().getString("TRADE_TITLE")))
            {
                e.setCancelled(true);

                String[] temp = DynamicShop.ccUser.get().getString(player.getUniqueId()+".interactItem").split("/");
                String shopName = temp[0];
                String tradeIdx = temp[1];

                if(e.getCurrentItem().getItemMeta() != null)
                {
                    // 닫기
                    if(e.getSlot() == 9)
                    {
                        DynaShopAPI.PlayerSoundEffect(player,"click");
                        DynamicShop.ccUser.get().set(player.getUniqueId().toString()+".interactItem","");

                        if(DynamicShop.ccUser.get().getString(player.getUniqueId() + ".tmpString").equalsIgnoreCase("sign"))
                        {
                            DynamicShop.ccUser.get().set(player.getUniqueId()+".tmpString","");
                            player.closeInventory();
                        }
                        else
                        {
                            DynaShopAPI.OpenShopGUI(player,shopName,1);
                        }
                    }
                    // 구매 또는 판매
                    else
                    {
                        // 잔액확인 버튼
                        if(e.getSlot() == 0)
                        {
                            DynaShopAPI.PlayerSoundEffect(player,"click");
                            if(DynamicShop.ccShop.get().contains(shopName+".Options.flag.jobpoint"))
                            {
                                player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().get("BALANCE") + ":§f " + df.format(DynaShopAPI.GetCurJobPoints(player)) + "Points");
                            }
                            else
                            {
                                player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().get("BALANCE") + ":§f $" + DynamicShop.getEconomy().format(DynamicShop.getEconomy().getBalance(player)));
                            }
                            return;
                        }

                        // 판매 토글
                        if(e.getSlot() == 1 || e.getSlot() == 8)
                        {
                            if(player.hasPermission("dshop.admin.shopedit"))
                            {
                                DynaShopAPI.PlayerSoundEffect(player,"click");
                                String path = shopName+"."+tradeIdx+".tradeType";
                                String tradeType = DynamicShop.ccShop.get().getString(path);
                                if(tradeType == null || !tradeType.equals("SellOnly"))
                                {
                                    DynamicShop.ccShop.get().set(path,"SellOnly");
                                }
                                else
                                {
                                    DynamicShop.ccShop.get().set(path,null);
                                }

                                DynaShopAPI.OpenItemTradeInven(player,shopName, tradeIdx);
                                DynamicShop.ccShop.save();
                            }
                            return;
                        }
                        // 구매 토글
                        if(e.getSlot() == 10 || e.getSlot() == 17)
                        {
                            if(player.hasPermission("dshop.admin.shopedit"))
                            {
                                DynaShopAPI.PlayerSoundEffect(player,"click");
                                String path = shopName+"."+tradeIdx+".tradeType";
                                String tradeType = DynamicShop.ccShop.get().getString(path);
                                if(tradeType == null || !tradeType.equals("BuyOnly"))
                                {
                                    DynamicShop.ccShop.get().set(path,"BuyOnly");
                                }
                                else
                                {
                                    DynamicShop.ccShop.get().set(path,null);
                                }

                                DynaShopAPI.OpenItemTradeInven(player,shopName, tradeIdx);
                                DynamicShop.ccShop.save();
                            }
                            return;
                        }

                        // 거래와 관련된 버튼들
                        int amount = e.getCurrentItem().getAmount();
                        double priceSum = 0;

                        ItemStack tempIS = new ItemStack(e.getCurrentItem().getType(),e.getCurrentItem().getAmount());
                        tempIS.setItemMeta((ItemMeta) DynamicShop.ccShop.get().get(shopName + "." + tradeIdx + ".itemStack"));

                        boolean infiniteStock = false;
                        // 무한재고&고정가격
                        if(DynamicShop.ccShop.get().getInt(shopName+"." + tradeIdx + ".stock") <= 0)
                        {
                            infiniteStock = true;
                        }

                        // 로컬샵이면 아에 창을 못열었고 딜리버리샵인데 월드가 다르면 배달불가.
                        ConfigurationSection optionS = DynamicShop.ccShop.get().getConfigurationSection(shopName).getConfigurationSection("Options");
                        int deliverycharge = 0;
                        if(optionS.contains("world") && optionS.contains("pos1") && optionS.contains("pos2") && optionS.contains("flag.deliverycharge"))
                        {
                            String lore = e.getCurrentItem().getItemMeta().getLore().toString();
                            if(lore.contains(DynamicShop.ccLang.get().getString("DELIVERYCHARGE")))
                            {
                                deliverycharge = Integer.parseInt(lore.split("\\$")[2].replace("]",""));
                                if(deliverycharge == -1)
                                {
                                    player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("DELIVERYCHARGE_NA"));
                                    return;
                                }
                                else
                                {
                                    if(e.getSlot()<=9)
                                    {
                                        priceSum -= deliverycharge;
                                    }
                                    else
                                    {
                                        priceSum += deliverycharge;
                                    }
                                }
                            }
                            else
                            {
                                deliverycharge = 0;
                            }
                        }

                        String permission = optionS.getString("permission");
                        // 판매
                        if(e.getSlot() <=9)
                        {
                            // 판매권한 확인
                            if(permission != null && permission.length()>0 && !player.hasPermission(permission) && !player.hasPermission(permission+".sell"))
                            {
                                player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("ERR.NO_PERMISSION"));
                                return;
                            }

                            if(optionS.contains("flag.jobpoint"))
                            {
                                SellItem_jobPoint(player,shopName,tradeIdx,tempIS,priceSum,deliverycharge,infiniteStock);
                            }
                            else
                            {
                                SellItem_cash(player,shopName,tradeIdx,tempIS,priceSum,deliverycharge,infiniteStock);
                            }
                        }
                        // 구매
                        else
                        {
                            // 구매 권한 확인
                            if(permission != null && permission.length()>0 && !player.hasPermission(permission) && !player.hasPermission(permission+".buy"))
                            {
                                player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("ERR.NO_PERMISSION"));
                                return;
                            }

                            if(optionS.contains("flag.jobpoint"))
                            {
                                BuyItem_jobPoint(player,shopName,tradeIdx,tempIS,priceSum,deliverycharge,infiniteStock);
                            }
                            else
                            {
                                BuyItem_cash(player,shopName,tradeIdx,tempIS,priceSum,deliverycharge,infiniteStock);
                            }
                        }
                    }
                }
            }
            // 파렛트 화면
            else if(e.getView().getTitle().equalsIgnoreCase(DynamicShop.ccLang.get().getString("PALETTE_TITLE")))
            {
                e.setCancelled(true);
                DynaShopAPI.PlayerSoundEffect(player,"click");

                String[] temp = DynamicShop.ccUser.get().getString(player.getUniqueId()+".interactItem").split("/");
                String shopName = temp[0];
                int curPage = e.getInventory().getItem(49).getAmount();

                // 닫기 버튼
                if(e.getSlot() == 45)
                {
                    DynamicShop.ccUser.get().set(player.getUniqueId().toString()+".interactItem","");
                    DynaShopAPI.OpenShopGUI(player,shopName,1);
                }
                // 페이지 버튼
                else if(e.getSlot() == 49)
                {
                    int targetPage = curPage;
                    if(e.isLeftClick())
                    {
                        targetPage -= 1;
                        if(targetPage<1)targetPage = 20;
                    }
                    else if(e.isRightClick())
                    {
                        targetPage += 1;
                        if(targetPage>20)targetPage = 1;
                    }
                    String search = e.getClickedInventory().getItem(53).getItemMeta().getLore().toString().replace("[","").replace("]","");
                    DynaShopAPI.OpenItemPalette(player,targetPage,search);
                }
                // 모두 추가 버튼
                else if(e.getSlot() == 51)
                {
                    for (int i = 0; i<45; i++)
                    {
                        if(e.getClickedInventory().getItem(i) != null && e.getClickedInventory().getItem(i).getType() != Material.AIR)
                        {
                            int existSlot = DynaShopAPI.FindItemFromShop(shopName,new ItemStack(e.getClickedInventory().getItem(i).getType()));
                            if(-1 == existSlot)
                            {
                                int idx = DynaShopAPI.FindEmptyShopSlot(shopName);
                                if(idx != -1)
                                {
                                    ItemStack tempIs = new ItemStack(e.getClickedInventory().getItem(i).getType());
                                    DynaShopAPI.AddItemToShop(shopName,idx,tempIs,1,0.01,-1,10000,10000);
                                }
                                else
                                {
                                    player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("ERR.NO_EMPTY_SLOT"));
                                    break;
                                }
                            } // 이미 상점에 추가되 있는 아이탬이라면 아무일도 안함.
                        }
                    }

                    DynaShopAPI.OpenShopGUI(player,shopName,1);
                }
                // 검색 버튼
                else if(e.getSlot() == 53)
                {
                    player.closeInventory();

                    DynamicShop.ccUser.get().set(player.getUniqueId()+".tmpString","waitforPalette");
                    OnChat.WaitForInput(player);

                    player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("SEARCH_ITEM"));
                }
                else if(e.getSlot()>45)
                {
                    return;
                }
                // 파렛트에서 뭔가 선택
                else
                {
                    if(e.getCurrentItem() != null && !e.getCurrentItem().getType().toString().equals(Material.AIR.toString()))
                    {
                        ItemStack iStack = new ItemStack(e.getCurrentItem().getType());
                        if(e.isLeftClick())
                        {
                            DynaShopAPI.OpenItemSettingGUI(player, iStack,1,10,0.01,-1,10000,10000);
                        }
                        else
                        {
                            DynaShopAPI.AddItemToShop(shopName,Integer.parseInt(temp[1]),iStack,-1,-1,-1,-1,-1);
                            DynaShopAPI.OpenShopGUI(player, shopName,Integer.parseInt(temp[1])/45+1);
                        }
                    }
                }
            }
            // 아이탬 설정 화면
            else if(e.getView().getTitle().equalsIgnoreCase(DynamicShop.ccLang.get().getString("ITEM_SETTING_TITLE")))
            {
                e.setCancelled(true);

                String[] temp = DynamicShop.ccUser.get().getString(player.getUniqueId()+".interactItem").split("/");
                String shopName = temp[0];

                if(e.getCurrentItem() == null)
                {
                    return;
                }

                // 닫기 버튼
                if(e.getSlot() == 27)
                {
                    DynaShopAPI.PlayerSoundEffect(player,"click");
                    DynaShopAPI.OpenItemPalette(player,1,"");
                    return;
                }
                //삭제 버튼
                else if(e.getSlot() == 35)
                {
                    int idx = DynaShopAPI.FindItemFromShop(shopName,e.getClickedInventory().getItem(0));
                    if (idx != -1)
                    {
                        DynaShopAPI.RemoveItemFromShop(shopName, idx);
                        player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("ITEM_DELETED"));
                        DynaShopAPI.OpenShopGUI(player,shopName,Integer.parseInt(temp[1])/45+1);
                        DynamicShop.ccUser.get().set(player.getUniqueId()+".interactItem","");
                        DynaShopAPI.PlayerSoundEffect(player,"deleteItem");
                        return;
                    }
                }

                String value = e.getClickedInventory().getItem(2).getItemMeta().getDisplayName();
                String valueMin = e.getClickedInventory().getItem(3).getItemMeta().getDisplayName();
                String valueMax = e.getClickedInventory().getItem(4).getItemMeta().getDisplayName();
                String median = e.getClickedInventory().getItem(5).getItemMeta().getDisplayName();
                String stock = e.getClickedInventory().getItem(6).getItemMeta().getDisplayName();

                value = value.replace(ChatColor.stripColor(DynamicShop.ccLang.get().getString("PRICE")),"");
                valueMin = valueMin.replace(ChatColor.stripColor(DynamicShop.ccLang.get().getString("PRICE_MIN")),"");
                valueMax = valueMax.replace(ChatColor.stripColor(DynamicShop.ccLang.get().getString("PRICE_MAX")),"");

                median = median.replace(ChatColor.stripColor(DynamicShop.ccLang.get().getString("MEDIAN")),"");
                stock = stock.replace(ChatColor.stripColor(DynamicShop.ccLang.get().getString("STOCK")),"");
                double valueD = Double.parseDouble(value);
                double valueMinD = Double.parseDouble(valueMin);
                if(valueMinD <= 0) valueMinD = 0.01;
                double valueMaxD = Double.parseDouble(valueMax);
                if(valueMaxD <= 0) valueMaxD = -1;
                int medianI = Integer.parseInt(median);
                int stockI = Integer.parseInt(stock);

                double newValue = valueD;
                double newValueMin = valueMinD;
                double newValueMax = valueMaxD;
                int newMedian = medianI;
                int newStock = stockI;

                int tab = 1;
                for (int i = 1; i<=5; i++)
                {
                    if(e.getClickedInventory().getItem(i+1).getType() == Material.RED_STAINED_GLASS_PANE)
                    {
                        tab = i;
                        break;
                    }
                }

                // 추천 버튼
                if(e.getSlot() == 31)
                {
                    String sug = e.getClickedInventory().getItem(0).getType().name();
                    double sugValue = DynamicShop.ccWorth.get().getDouble(sug);

                    if(sugValue == 0)
                    {
                        player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("ERR.NO_RECOMMAND_DATA"));
                    }
                    else
                    {
                        int pnum = DynamicShop.plugin.getConfig().getInt("NumberOfPlayer");
                        int sugMid = (int)(4/(Math.pow(sugValue,0.35))*1000*pnum);

                        player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("RECOMMAND_APPLIED").replace("{playerNum}",pnum+""));

                        DynaShopAPI.OpenItemSettingGUI(player,e.getInventory().getItem(0),tab,
                                sugValue,newValueMin,newValueMax,sugMid,sugMid);

                        DynaShopAPI.PlayerSoundEffect(player,"editItem");
                    }
                    return;
                }

                // 가격,미디언,스톡 탭 이동
                if(e.getSlot() >= 2 && e.getSlot() <= 6)
                {
                    DynaShopAPI.PlayerSoundEffect(player,"click");
                    DynaShopAPI.OpenItemSettingGUI(player,e.getClickedInventory().getItem(0),e.getSlot()-1,valueD,valueMinD,valueMaxD,medianI,stockI);
                }
                // + - 버튼들
                else if(e.getSlot() >= 9 && e.getSlot() < 18)
                {
                    if(e.getSlot() != 13)
                    {
                        String s = e.getCurrentItem().getItemMeta().getDisplayName();
                        double editNum = Double.parseDouble(s);
                        if(e.isShiftClick()) editNum *= 5f;

                        if(tab == 1)
                        {
                            newValue = valueD + editNum;
                            if(newValue < 0.01) newValue = 0.01f;
                        }
                        if(tab == 2)
                        {
                            if(valueMinD <= 0.01) valueMinD = 0;
                            newValueMin = valueMinD + editNum;
                            if(newValueMin < 0.01) newValueMin = 0.01f;
                        }
                        if(tab == 3)
                        {
                            if(valueMaxD < 0) valueMaxD = 0;
                            newValueMax = valueMaxD + editNum;
                            if(newValueMax < -1) newValueMax = -1;
                        }
                        else if(tab == 4)
                        {
                            newMedian = medianI + (int)editNum;
                            if(newMedian < -1) newMedian = -1;
                        }
                        else if(tab == 5)
                        {
                            newStock = stockI + (int)editNum;
                            if(newStock < -1) newStock = -1;
                        }

                        DynaShopAPI.OpenItemSettingGUI(player,e.getInventory().getItem(0),tab,newValue,newValueMin,newValueMax,newMedian,newStock);
                    }
                    else
                    {
                        if(tab == 1)
                        {
                            newValue = 10;
                        }
                        else if(tab == 2)
                        {
                            newValueMin = 0.01;
                        }
                        else if(tab == 3)
                        {
                            newValueMax = -1;
                        }
                        else if(tab == 4)
                        {
                            newMedian = 10000;
                        }
                        else if(tab == 5)
                        {
                            newStock = 10000;
                        }

                        DynaShopAPI.OpenItemSettingGUI(player,e.getInventory().getItem(0),tab,newValue,newValueMin,newValueMax,newMedian,newStock);
                    }
                    DynaShopAPI.PlayerSoundEffect(player,"editItem");
                }
                // 나누기
                else if(e.getSlot() == 21)
                {
                    int div = 2;
                    if(e.isShiftClick()) div = 10;
                    if(tab == 1)
                    {
                        if(valueD<=0.01)return;
                        newValue = valueD/div;
                    }
                    if(tab == 2)
                    {
                        if(valueMinD<=0.01)return;
                        newValueMin = valueMinD/div;
                    }
                    if(tab == 3)
                    {
                        if(valueMaxD<=0.01)return;
                        newValueMax = valueMaxD/div;
                    }
                    else if(tab == 4)
                    {
                        if(medianI<=1)return;
                        newMedian = medianI/div;
                    }
                    else if(tab == 5)
                    {
                        if(stockI<=1)return;
                        newStock = stockI/div;
                    }

                    DynaShopAPI.OpenItemSettingGUI(player,e.getInventory().getItem(0),tab,newValue,newValueMin,newValueMax,newMedian,newStock);
                    DynaShopAPI.PlayerSoundEffect(player,"editItem");
                }
                // 곱하기
                else if(e.getSlot() == 23)
                {
                    int mul = 2;
                    if(e.isShiftClick()) mul = 10;
                    if(tab == 1)
                    {
                        if(valueD<=0)return;
                        newValue = valueD*mul;
                    }
                    else if(tab == 2)
                    {
                        if(valueMinD<=0)return;
                        newValueMin = valueMinD*mul;
                    }
                    else if(tab == 3)
                    {
                        if(valueMaxD<=0)return;
                        newValueMax = valueMaxD*mul;
                    }
                    else if(tab == 4)
                    {
                        if(medianI<=0)return;
                        newMedian = medianI*mul;
                    }
                    else if(tab == 5)
                    {
                        if(stockI<=0)return;
                        newStock = stockI*mul;
                    }

                    DynaShopAPI.OpenItemSettingGUI(player,e.getInventory().getItem(0),tab,newValue,newValueMin,newValueMax,newMedian,newStock);
                    DynaShopAPI.PlayerSoundEffect(player,"editItem");
                }
                // 내림 버튼
                else if(e.getSlot() == 20)
                {
                    if(tab == 1)
                    {
                        newValue = DynaShopAPI.RoundDown((int)valueD);
                    }
                    else if(tab == 2)
                    {
                        newValueMin = DynaShopAPI.RoundDown((int)valueMinD);
                    }
                    else if(tab == 3)
                    {
                        newValueMax = DynaShopAPI.RoundDown((int)valueMaxD);
                    }
                    else if(tab == 4)
                    {
                        newMedian = DynaShopAPI.RoundDown(medianI);
                    }
                    else if(tab == 5)
                    {
                        newStock = DynaShopAPI.RoundDown(stockI);
                    }

                    DynaShopAPI.OpenItemSettingGUI(player,e.getInventory().getItem(0),tab,newValue,newValueMin,newValueMax,newMedian,newStock);
                    DynaShopAPI.PlayerSoundEffect(player,"editItem");
                }
                // 스톡을 미디안에 맞춤. 또는 그 반대
                else if(e.getSlot() == 24)
                {
                    if(tab == 2)
                    {
                        newValueMin = valueD;
                    }
                    else if(tab == 3)
                    {
                        newValueMax = valueD;
                    }
                    else if(tab == 4)
                    {
                        newMedian = stockI;
                    }
                    else if(tab == 5)
                    {
                        newStock = medianI;
                    }

                    DynaShopAPI.OpenItemSettingGUI(player,e.getInventory().getItem(0),tab,newValue,newValueMin,newValueMax,newMedian,newStock);
                    DynaShopAPI.PlayerSoundEffect(player,"editItem");
                }
                //완료
                else if(e.getSlot() == 8)
                {
                    // 유효성 검사
                    if(valueMaxD > 0 && valueD > valueMaxD)
                    {
                        player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("ERR.DEFAULT_VALUE_OUT_OF_RANGE"));
                        return;
                    }
                    if(valueMinD > 0 && valueD < valueMinD)
                    {
                        player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("ERR.DEFAULT_VALUE_OUT_OF_RANGE"));
                        return;
                    }
                    if(valueMaxD > 0 && valueMinD > 0 && valueMinD >= valueMaxD)
                    {
                        player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("ERR.MAX_LOWER_THAN_MIN"));
                        return;
                    }

                    int existSlot = DynaShopAPI.FindItemFromShop(shopName,e.getClickedInventory().getItem(0));
                    if(-1 != existSlot)
                    {
                        DynaShopAPI.EditShopItem(shopName,existSlot,valueD,valueMinD,valueMaxD,medianI,stockI);
                        DynaShopAPI.OpenShopGUI(player,shopName,existSlot/45+1);
                        DynamicShop.ccUser.get().set(player.getUniqueId()+".interactItem","");
                        DynaShopAPI.PlayerSoundEffect(player,"addItem");
                    }
                    else
                    {
                        int idx = DynaShopAPI.FindEmptyShopSlot(shopName);

                        try
                        {
                            idx = Integer.parseInt(DynamicShop.ccUser.get().getString(player.getUniqueId()+".interactItem").split("/")[1]);
                        }catch (Exception exception)
                        {

                        }

                        if(idx != -1)
                        {
                            DynaShopAPI.AddItemToShop(shopName,idx,e.getClickedInventory().getItem(0),valueD,valueMinD,valueMaxD,medianI,stockI);
                            DynaShopAPI.OpenShopGUI(player,shopName,Integer.parseInt(temp[1])/45+1);
                            DynamicShop.ccUser.get().set(player.getUniqueId()+".interactItem","");
                            DynaShopAPI.PlayerSoundEffect(player,"addItem");
                        }
                    }
                }
            }
        }
        // 퀵셀
        else if(e.getView().getTitle().equalsIgnoreCase(DynamicShop.ccLang.get().getString("QUICKSELL_TITLE")))
        {
            e.setCancelled(true);

            if(e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR)
            {
                return;
            }

            String topShopName = "";
            double topPrice = -1;
            int tradeIdx = -1;

            // 접근가능한 상점중 최고가 찾기
            for (String shop:DynamicShop.ccShop.get().getKeys(false))
            {
                ConfigurationSection shopConf = DynamicShop.ccShop.get().getConfigurationSection(shop);

                // 권한 없는 상점
                String permission = shopConf.getString("Options.permission");
                if(permission != null && permission.length()>0 && !player.hasPermission(permission) && !player.hasPermission(permission+".sell"))
                {
                    continue;
                }

                // 표지판 전용 상점, 지역상점, 잡포인트 상점
                if(shopConf.contains("Options.flag.localshop") || shopConf.contains("Options.flag.signshop") || shopConf.contains("Options.flag.jobpoint")) continue;

                int sameItemIdx = DynaShopAPI.FindItemFromShop(shop,e.getCurrentItem());

                if(sameItemIdx != -1)
                {
                    String tradeType = shopConf.getString(sameItemIdx+".tradeType");
                    if(tradeType != null && tradeType.equals("BuyOnly")) continue; // 구매만 가능함

                    // 상점에 돈이 없음
                    if(DynaShopAPI.GetShopBalance(shop) != -1 && DynaShopAPI.GetShopBalance(shop) < DynaShopAPI.CalcTotalCost(shop,String.valueOf(sameItemIdx),e.getCurrentItem().getAmount()))
                    {
                        continue;
                    }

                    double value = shopConf.getDouble(sameItemIdx+".value");

                    int tax = DynamicShop.plugin.getConfig().getInt("SalesTax");
                    if(shopConf.contains("Options.SalesTax"))
                    {
                        tax = shopConf.getInt("Options.SalesTax");
                    }

                    if(topPrice <  value - ((value / 100) * tax))
                    {
                        topShopName = shop;
                        topPrice = shopConf.getDouble(sameItemIdx+".value");
                        tradeIdx = sameItemIdx;
                    }
                }
            }

            if(topShopName.length()>0)
            {
                // 찾은 상점에 판매
                DynaShopAPI.QuickSellItem(player,e.getCurrentItem(),topShopName,tradeIdx);
                player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("QSELL_RESULT")+topShopName);
            }
            else
            {
                player.sendMessage(DynamicShop.dsPrefix +DynamicShop.ccLang.get().getString("QSELL_NA")+topShopName);
            }
        }
        // Shift클릭으로 UI인벤에 아이탬 올리는것 막기
        else if(e.isShiftClick())
        {
            if(DynamicShop.ccShop.get().contains(ChatColor.stripColor(e.getView().getTitle())))
            {
                if(e.getInventory().getSize() == 54 && e.getInventory().getItem(53).getItemMeta().getDisplayName().
                        equals(DynamicShop.ccLang.get().getString("SHOP_INFO")))
                {
                    e.setCancelled(true);
                }
            }
            else if(e.getView().getTitle().equalsIgnoreCase(DynamicShop.ccLang.get().getString("TRADE_TITLE"))) e.setCancelled(true);
            else if (e.getView().getTitle().equalsIgnoreCase(DynamicShop.ccLang.get().getString("PALETTE_TITLE"))) { e.setCancelled(true); }
            else if (e.getView().getTitle().equalsIgnoreCase(DynamicShop.ccLang.get().getString("SHOP_SETTING_TITLE"))) { e.setCancelled(true); }
            else if (e.getView().getTitle().equalsIgnoreCase(DynamicShop.ccLang.get().getString("ITEM_SETTING_TITLE"))) { e.setCancelled(true); }
            else if (e.getView().getTitle().equalsIgnoreCase(DynamicShop.ccStartpage.get().getString("Options.Title"))) { e.setCancelled(true); }
            else if (e.getView().getTitle().equalsIgnoreCase(DynamicShop.ccLang.get().getString("QUICKSELL_TITLE"))) { e.setCancelled(true); }
            else if (e.getView().getTitle().equalsIgnoreCase(DynamicShop.ccLang.get().getString("STARTPAGE.EDITOR_TITLE"))) { e.setCancelled(true); }
        }
        // 팔렛트 표시중에 자기인벤을 클릭
        else if(e.getView().getTitle().equalsIgnoreCase(DynamicShop.ccLang.get().getString("PALETTE_TITLE")))
        {
            e.setCancelled(true);
            if(e.getCurrentItem() != null && !e.getCurrentItem().getType().toString().equals(Material.AIR.toString()))
            {
                if(e.isLeftClick())
                {
                    DynaShopAPI.OpenItemSettingGUI(player, e.getCurrentItem(),1,10,0.01,-1,1000,1000);
                }
                else
                {
                    String[] temp = DynamicShop.ccUser.get().getString(player.getUniqueId()+".interactItem").split("/");

                    DynaShopAPI.AddItemToShop(temp[0],Integer.parseInt(temp[1]),e.getCurrentItem(),-1,-1,-1,-1,-1);
                    DynaShopAPI.OpenShopGUI(player, temp[0],Integer.parseInt(temp[1])/45+1);
                }
            }
        }
    }

    //========================================================================================

    private void SellItem_cash(Player player, String shopName, String tradeIdx, ItemStack tempIS, double priceSum, double deliverycharge, boolean infiniteStock)
    {
        // 상점에 돈이 없음
        if(DynaShopAPI.GetShopBalance(shopName) != -1 && DynaShopAPI.GetShopBalance(shopName) < DynaShopAPI.CalcTotalCost(shopName,tradeIdx,tempIS.getAmount()))
        {
            player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("SHOP_BAL_LOW"));
            return;
        }

        // 실제 판매 가능량 확인
        int actualAmount = tempIS.getAmount();
        HashMap<Integer,ItemStack> hashMap = player.getInventory().removeItem(tempIS);
        player.updateInventory();
        if(!hashMap.isEmpty())
        {
            actualAmount -= hashMap.get(0).getAmount();
        }

        // 판매할 아이탬이 없음
        if(actualAmount == 0)
        {
            player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("NO_ITEM_TO_SELL"));
            return;
        }

        priceSum += DynaShopAPI.CalcTotalCost(shopName,tradeIdx,-actualAmount);

        // 재고 증가
        if(!infiniteStock)
        {
            DynamicShop.ccShop.get().set(shopName+"." + tradeIdx + ".stock",
                    DynamicShop.ccShop.get().getInt(shopName+"." + tradeIdx + ".stock") + actualAmount);
        }

        // 면제된 배달비 계산용
        double oldPriceSum = 0;
        if(priceSum <= 0)
        {
            oldPriceSum = priceSum;
            priceSum = 0;
        }

        // 실제 거래부----------
        Economy econ = DynamicShop.getEconomy();
        EconomyResponse r = DynamicShop.getEconomy().depositPlayer(player, priceSum);

        if(r.transactionSuccess())
        {
            player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("SELL_SUCCESS")
                    .replace("{item}",tempIS.getType().name())
                    .replace("{amount}", Integer.toString(actualAmount))
                    .replace("{price}",econ.format(r.amount))
                    .replace("{bal}",econ.format(econ.getBalance((player)))));
            DynaShopAPI.PlayerSoundEffect(player,"sell");

            if(deliverycharge > 0)
            {
                if(priceSum == 0)
                {
                    player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("DELIVERYCHARGE_EXEMPTION").
                            replace("{fee}",""+deliverycharge).replace("{fee2}",(oldPriceSum-priceSum)*-1+""));
                }
                else
                {
                    player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("DELIVERYCHARGE") + ": $" + deliverycharge);
                }
            }

            if(DynamicShop.ccShop.get().contains(shopName+".Options.Balance"))
            {
                DynaShopAPI.AddShopBalance(shopName, priceSum * -1);
            }

            DynamicShop.ccShop.save();
            DynaShopAPI.OpenItemTradeInven(player, shopName, tradeIdx);
        } else {
            player.sendMessage(String.format("[Vault] An error occured: %s", r.errorMessage));
        }
    }

    private void BuyItem_cash(Player player, String shopName, String tradeIdx, ItemStack tempIS, double priceSum, double deliverycharge, boolean infiniteStock)
    {
        Economy econ = DynamicShop.getEconomy();

        int actualAmount = 0;
        int stockOld = DynamicShop.ccShop.get().getInt(shopName+"." + tradeIdx + ".stock");

        for (int i = 0; i<tempIS.getAmount(); i++)
        {
            if(!infiniteStock && stockOld <= actualAmount+1)
            {
                break;
            }

            double price = DynaShopAPI.GetCurrentValue(shopName,tradeIdx);

            if(priceSum + price > econ.getBalance(player)) break;

            priceSum += price;

            if(!infiniteStock)
            {
                DynamicShop.ccShop.get().set(shopName+"." + tradeIdx + ".stock",
                        DynamicShop.ccShop.get().getInt(shopName+"." + tradeIdx + ".stock") - 1);
            }

            actualAmount++;
        }

        // 실 구매 가능량이 0이다 = 돈이 없다.
        if(actualAmount <= 0)
        {
            player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("NOT_ENOUGH_MONEY").replace("{bal}",econ.format(econ.getBalance(player))));
            DynamicShop.ccShop.get().set(shopName+"." + tradeIdx + ".stock", stockOld);
            return;
        }

        // 상점 재고 부족
        if(!infiniteStock && stockOld <= actualAmount)
        {
            player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("OUT_OF_STOCK"));
            DynamicShop.ccShop.get().set(shopName+"." + tradeIdx + ".stock", stockOld);
            return;
        }

        // 실 거래부-------
        if(econ.getBalance(player) >= priceSum)
        {
            EconomyResponse r = DynamicShop.getEconomy().withdrawPlayer(player, priceSum);

            if(r.transactionSuccess())
            {
                int leftAmount = actualAmount;
                while (leftAmount>0)
                {
                    int giveAmount = tempIS.getType().getMaxStackSize();
                    if(giveAmount > leftAmount) giveAmount = leftAmount;

                    ItemStack iStack = new ItemStack(tempIS.getType(),giveAmount);
                    iStack.setItemMeta((ItemMeta) DynamicShop.ccShop.get().get(shopName + "." + tradeIdx + ".itemStack"));

                    HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(iStack);
                    if(leftOver.size() != 0)
                    {
                        player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("INVEN_FULL"));
                        Location loc = player.getLocation();

                        ItemStack leftStack = new ItemStack(tempIS.getType(),leftOver.get(0).getAmount());
                        leftStack.setItemMeta((ItemMeta) DynamicShop.ccShop.get().get(shopName + "." + tradeIdx + ".itemStack"));

                        player.getWorld().dropItem(loc, leftStack);
                    }

                    leftAmount -= giveAmount;
                }

                player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("BUY_SUCCESS")
                        .replace("{item}",tempIS.getType().name())
                        .replace("{amount}", Integer.toString(actualAmount))
                        .replace("{price}",econ.format(r.amount))
                        .replace("{bal}",econ.format(econ.getBalance((player)))));
                DynaShopAPI.PlayerSoundEffect(player,"buy");

                if(deliverycharge > 0)
                {
                    player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("DELIVERYCHARGE")+": $"+deliverycharge);
                }

                if(DynamicShop.ccShop.get().contains(shopName+".Options.Balance"))
                {
                    DynaShopAPI.AddShopBalance(shopName,priceSum);
                }

                DynaShopAPI.OpenItemTradeInven(player,shopName, tradeIdx);
                DynamicShop.ccShop.save();
            }
            else
            {
                player.sendMessage(String.format("An error occured: %s", r.errorMessage));
            }
        }
        else
        {
            player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("NOT_ENOUGH_MONEY").replace("{bal}",econ.format(econ.getBalance(player))));
        }
    }

    private void SellItem_jobPoint(Player player, String shopName, String tradeIdx, ItemStack tempIS, double priceSum, double deliverycharge, boolean infiniteStock)
    {
        // 상점에 돈이 없음
        if(DynaShopAPI.GetShopBalance(shopName) != -1 && DynaShopAPI.GetShopBalance(shopName) < DynaShopAPI.CalcTotalCost(shopName,tradeIdx,tempIS.getAmount()))
        {
            player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("SHOP_BAL_LOW"));
            return;
        }

        // 실제 판매 가능량 확인
        int actualAmount = tempIS.getAmount();
        HashMap<Integer,ItemStack> hashMap = player.getInventory().removeItem(tempIS);
        player.updateInventory();
        if(!hashMap.isEmpty())
        {
            actualAmount -= hashMap.get(0).getAmount();
        }

        // 판매할 아이탬이 없음
        if(actualAmount == 0)
        {
            player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("NO_ITEM_TO_SELL"));
            return;
        }

        priceSum += DynaShopAPI.CalcTotalCost(shopName,tradeIdx,-actualAmount);

        // 재고 증가
        if(!infiniteStock)
        {
            DynamicShop.ccShop.get().set(shopName+"." + tradeIdx + ".stock",
                    DynamicShop.ccShop.get().getInt(shopName+"." + tradeIdx + ".stock") + actualAmount);
        }

        // 면제된 배달비 계산용
        double oldPriceSum = 0;
        if(priceSum <= 0)
        {
            oldPriceSum = priceSum;
            priceSum = 0;
        }

        // 실제 거래부----------
        if(DynaShopAPI.AddJobsPoint(player, priceSum))
        {
            player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("SELL_SUCCESS_JP")
                    .replace("{item}",tempIS.getType().name())
                    .replace("{amount}", Integer.toString(actualAmount))
                    .replace("{price}",df.format(priceSum))
                    .replace("{bal}",df.format(DynaShopAPI.GetCurJobPoints(player))));
            DynaShopAPI.PlayerSoundEffect(player,"sell");

            if(deliverycharge > 0)
            {
                if(priceSum == 0)
                {
                    player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("DELIVERYCHARGE_EXEMPTION").
                            replace("{fee}",""+deliverycharge).replace("{fee2}",(oldPriceSum-priceSum)*-1+""));
                }
                else
                {
                    player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("DELIVERYCHARGE") + ": " + deliverycharge);
                }
            }

            if(DynamicShop.ccShop.get().contains(shopName+".Options.Balance"))
            {
                DynaShopAPI.AddShopBalance(shopName, priceSum * -1);
            }

            DynamicShop.ccShop.save();
            DynaShopAPI.OpenItemTradeInven(player, shopName, tradeIdx);
        }
    }

    private void BuyItem_jobPoint(Player player, String shopName, String tradeIdx, ItemStack tempIS, double priceSum, double deliverycharge, boolean infiniteStock)
    {
        int actualAmount = 0;
        int stockOld = DynamicShop.ccShop.get().getInt(shopName+"." + tradeIdx + ".stock");

        for (int i = 0; i<tempIS.getAmount(); i++)
        {
            if(!infiniteStock && stockOld <= actualAmount+1)
            {
                break;
            }

            double price = DynaShopAPI.GetCurrentValue(shopName,tradeIdx);

            if(priceSum + price > DynaShopAPI.GetCurJobPoints(player)) break;

            priceSum += price;

            if(!infiniteStock)
            {
                DynamicShop.ccShop.get().set(shopName+"." + tradeIdx + ".stock",
                        DynamicShop.ccShop.get().getInt(shopName+"." + tradeIdx + ".stock") - 1);
            }

            actualAmount++;
        }

        // 실 구매 가능량이 0이다 = 돈이 없다.
        if(actualAmount <= 0)
        {
            player.sendMessage(DynamicShop.dsPrefix+DynamicShop.ccLang.get().getString("NOT_ENOUGH_POINT").replace("{bal}", df.format(DynaShopAPI.GetCurJobPoints(player))));
            DynamicShop.ccShop.get().set(shopName+"." + tradeIdx + ".stock", stockOld);
            return;
        }

        // 상점 재고 부족
        if(!infiniteStock && stockOld <= actualAmount)
        {
            player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("OUT_OF_STOCK"));
            DynamicShop.ccShop.get().set(shopName+"." + tradeIdx + ".stock", stockOld);
            return;
        }

        // 실 거래부-------
        if(DynaShopAPI.GetCurJobPoints(player) >= priceSum)
        {
            if(DynaShopAPI.AddJobsPoint(player,priceSum * -1))
            {
                int leftAmount = actualAmount;
                while (leftAmount>0)
                {
                    int giveAmount = tempIS.getType().getMaxStackSize();
                    if(giveAmount > leftAmount) giveAmount = leftAmount;

                    ItemStack iStack = new ItemStack(tempIS.getType(),giveAmount);
                    iStack.setItemMeta((ItemMeta) DynamicShop.ccShop.get().get(shopName + "." + tradeIdx + ".itemStack"));

                    HashMap<Integer, ItemStack> leftOver = player.getInventory().addItem(iStack);
                    if(leftOver.size() != 0)
                    {
                        player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("INVEN_FULL"));
                        Location loc = player.getLocation();

                        ItemStack leftStack = new ItemStack(tempIS.getType(),leftOver.get(0).getAmount());
                        leftStack.setItemMeta((ItemMeta) DynamicShop.ccShop.get().get(shopName + "." + tradeIdx + ".itemStack"));

                        player.getWorld().dropItem(loc, leftStack);
                    }

                    leftAmount -= giveAmount;
                }

                player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("BUY_SUCCESS_JP")
                        .replace("{item}",tempIS.getType().name())
                        .replace("{amount}", Integer.toString(actualAmount))
                        .replace("{price}",df.format(priceSum))
                        .replace("{bal}",df.format(DynaShopAPI.GetCurJobPoints((player)))));
                DynaShopAPI.PlayerSoundEffect(player,"buy");

                if(deliverycharge > 0)
                {
                    player.sendMessage(DynamicShop.dsPrefix + DynamicShop.ccLang.get().getString("DELIVERYCHARGE")+": "+deliverycharge);
                }

                if(DynamicShop.ccShop.get().contains(shopName+".Options.Balance"))
                {
                    DynaShopAPI.AddShopBalance(shopName,priceSum);
                }

                DynaShopAPI.OpenItemTradeInven(player,shopName, tradeIdx);
                DynamicShop.ccShop.save();
            }
        }
    }
}