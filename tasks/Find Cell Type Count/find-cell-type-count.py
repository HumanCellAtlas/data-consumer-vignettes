#!/usr/bin/env python3

import hca, json


def count_cell_types(cell_type):
    count = 0
    query = {
        "query" : {
            "bool" : {
                "must" : [{
                    "match" : {
                        "metadata.files.biomaterial_json.biomaterials.context.organ.text" : "spleen"
                    }
                }, {
                    "match" : {
                        "metadata.files.biomaterial_json.schema_version" : "5.1.0"
                    }
                }, {
                    "range" : {
                        "metadata.files.biomaterial_json.biomaterials.content.total_estimated_cells" : {
                            "gt" : 0
                        }
                    }
                }]
            }
        }
    }
    
#    query = { "query" : { "bool" : { "must" : { "match" : { "*" : "*" } } } } }
    
#    query = { "query" : { "match" : { "search_score" : 1.0 } } }
    
    query = { "match" : { "search_score" : 1.0 } }
    
    # Iterate through all search results containing the chosen cell type
    for bundle_dict in hca.dss.DSSClient().post_search.iterate(es_query=query, replica='aws', output_format='raw'):
        
        # Check if the bundle is missing information. If so, skip to next iteration
        if not bundle_dict['metadata'].get('files'):
            continue
        
        print(json.dumps(bundle_dict, indent=4, sort_keys=True))
        print()
        
        # If the checks passed, then increment the count by the number of cells found in this file
        count += bundle_dict['metadata']['files']['biomaterial_json']['biomaterials'][0]['content']['total_estimated_cells']
        print(count)
        break
        
    return count


def main():
    organ_type = 'spleen'
    print('{} cell count: {}'.format(organ_type, count_cell_types(organ_type)))

    
main()
