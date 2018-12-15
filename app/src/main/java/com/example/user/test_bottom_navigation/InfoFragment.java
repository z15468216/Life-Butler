package com.example.user.test_bottom_navigation;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InfoFragment extends Fragment {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_info_fragment, null);

        expListView = view.findViewById(R.id.list);

        // preparing list data
        prepareListData();

        listAdapter = new com.example.user.test_bottom_navigation.ExpandableListAdapter(getContext(), listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                new AlertDialog.Builder(getActivity())
                        .setMessage(listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }).show();
                return true;
            }
        });
        return view;
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        // Adding child data
        listDataHeader.add("有關氣體的小知識");
        listDataHeader.add("有關噪音的小知識");

        // Adding child data
        List<String> GAS = new ArrayList<>();
        GAS.add("一氧化碳( CO )對於體內血紅蛋白的親和力比起氧氣( O2 )對於血紅蛋白的親和力大200 - 300倍，而碳氧血紅蛋白( 攜帶一氧化碳的紅血球 )較氧合血紅蛋白( 攜帶氧氣的紅血球 )的解離速度慢3600倍，當一氧化碳濃度在空氣中達到35ppm時，就會對人體產生損害，會造成一氧化碳中毒（又稱煤氣中毒）。");
        GAS.add( "血中一氧化碳含量（血紅蛋白飽和度%）會出現的症狀\n" +
                "> 10%後，心理與體力工作可能受到影響\n" +
                "> 20%-30%->也許會有輕微和持續抽痛式的頭痛、虛弱，一用力就會昏倒\n" +
                "> 30%-40%->腦里陣陣抽痛和很脹的感覺，嚴重頭疼、軟弱無力、噁心、不處理就會覺得虛弱、血壓下降、脈搏加快、嘔吐\n" +
                "> 40%-60%->同上，加上虛弱和身心不協調、心神混亂、無法行走\n" +
                "> 60%->喪失意識、抽搐、死亡，除非很快移出中毒環境\n" +
                "> 80%->快速死亡，除非及時救治。");
        GAS.add(  "一氧化碳對人體傷害程度的症狀：\n" +
                "0.01%(100ppm)-> 在2~3 小時內會輕微頭痛\n" +
                "0.04%(400ppm)-> 在1~2 小時內前額頭痛2.5 小時到3.5 小時會蔓延\n" +
                "0.16%(1600ppm)-> 20 分鐘內會頭痛,暈旋。2 小時會死亡\n" +
                "0.32%(3200ppm)-> 5~10 分鐘會頭痛暈旋,嘔吐。30 分鐘會死亡\n" +
                "0.64%(6400ppm)-> 1~2 分鐘內會頭痛,暈旋。10~15 鍾內會死亡\n" +
                "1.28%(12800ppm)-> 1~3 分鐘會死亡");

        List<String> NOISE = new ArrayList<>();
        NOISE.add("長期處在噪音環境不但易耳聾．還會引發疾病影響生育或猝死。");
        NOISE.add("毫無防護地置身於80分貝以上的噪音當中，就會使聽覺細胞逐漸受損，造成耳聾。若突然發生120分貝以上的噪音，如大砲聲、爆炸聲、鑿岩機聲等，可能立即導致耳聾，不可不慎。");
        NOISE.add("長期生活在70分貝至80分貝以上的環境中，可使人動脈收縮 、心跳加速、供血不足、出現血壓不穩、心律不整、心悸等症狀，甚至演變成冠心病，心絞痛、腦溢血及心肌梗塞。研究指出，噪音強度每升高5分貝，罹患高血壓的機率，就可能提高約20%破壞人體正常運作");
        NOISE.add("噪音可能使中樞神經功能失調、大腦皮質興奮及抑制功能失去平衡，導致身體出現失眠、多夢、頭痛、耳鳴、全身乏力等現象。");
        NOISE.add("噪音會使人腎上腺素分泌增加，以致容易驚慌、恐懼、易怒、焦躁，甚至演變成神經衰弱、憂鬱或精神分裂症。");
        NOISE.add("噪音會引起消化系統功能障礙、內分泌失調，使人出現食慾不振、消化不良、腸胃衰弱、噁心、嘔吐等症狀，最後還可能導致消化道潰瘍、肝硬化等疾病的產生。");
        NOISE.add("在噪音80分貝以上的環境中工作、學習，將使人精神無法集中、聽力下降，降低工作、學習效率。");
        NOISE.add("噪音會使微血管收縮，減低血液中活性氧流通，造成精神緊張亢奮、情緒無法安定。");
        NOISE.add("噪音會對婦女的月經和生育功能產生影響，使婦女出現月經不規則、痛經等現象。而噪音還會使孕婦產生妊娠惡阻、妊娠高血壓及產下低體重兒等危機，甚至造成流產、早產。");
        NOISE.add("胎兒和幼兒的聽覺神經敏感脆弱，極易受噪音的破壞，嚴重時甚至會影響智力的發展。");
        NOISE.add("美國醫學專家研究指出，突發的強烈噪音，可使聽覺受到刺激，引起突發性的心律不整，使人猝死。");

        listDataChild.put(listDataHeader.get(0), GAS); // Header, Child data
        listDataChild.put(listDataHeader.get(1), NOISE);
    }

}

